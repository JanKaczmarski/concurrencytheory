package lab11

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select

/*
[
  FILOZOF(i: 0..4) ::
    *[ true ->
         mysli;

         Lokaj!Chce();

         Widelec(i)!Chce();
         Widelec((i+1) mod 5)!Chce();
         je;
         Widelec((i+1) mod 5)!Odkłada();
         Widelec(i)!Odkłada();

         Lokaj!Wychodzę()
     ]

  ||

  WIDELEC(i: 0..4) ::
    *[
       Filozof(i)?Chce() ->
         Filozof(i)?Odklada()

    [] Filozof((i+4) mod 5)?Chce() ->
         Filozof((i+4) mod 5)?Odklada()
     ]

  ||

  LOKAJ ::
    wolnych: integer;
    wolnych := 4;

    *[
       (i:0..4) wolnych > 0; Filozof(i)?Chce() ->
         wolnych := wolnych - 1

    [] (i:0..4) Filozof(i)?Wychodze() ->
         wolnych := wolnych + 1
     ]
]
 */

fun main() = runBlocking {
    println("ZADANIE 2: Pięciu Filozofów z Lokajem (Brak blokady)")
    val N = 5

    val forkChannels = Array(N) { i -> Array(N) { Channel<Sygnal>(Channel.RENDEZVOUS) } }

    // Kanały do komunikacji z Lokajem
    val butlerEnter = Channel<Sygnal>(Channel.RENDEZVOUS)
    val butlerExit = Channel<Sygnal>(Channel.RENDEZVOUS)

    // Lokaj
    launch {
        var wolnych = 4 // Lokaj wpuszcza max 4 osoby (N-1)
        println("[LOKAJ] Startuje. Wolnych miejsc: $wolnych")

        while (isActive) {
            // CSP: *[(i:0..4) wolnych > 0; Filozof(i)?Chce() -> ... | ...]
            select<Unit> {
                if (wolnych > 0) {
                    butlerEnter.onReceive {
                        wolnych--
                        println("[LOKAJ] Wpuszczono filozofa. Wolnych: $wolnych")
                    }
                }

                butlerExit.onReceive {
                    wolnych++
                    println("[LOKAJ] Pożegnanie. Wolnych: $wolnych")
                }
            }
        }
    }

    // Widelce
    for (i in 0 until N) {
        launch {
            val me = i
            val leftId = i
            val rightId = (i + 4) % N
            val chLeft = forkChannels[leftId][me]
            val chRight = forkChannels[rightId][me]

            while (isActive) {
                select<Unit> {
                    chLeft.onReceive { if (it == Sygnal.CHCE) chLeft.receive() }
                    chRight.onReceive { if (it == Sygnal.CHCE) chRight.receive() }
                }
            }
        }
    }

    // Filozofowie
    for (i in 0 until N) {
        launch {
            val me = i
            val leftFork = forkChannels[me][i]
            val rightFork = forkChannels[me][(i + 1) % N]

            while (isActive) {
                // CSP: mysli
                println("Filozof $me: Myśli...")
                delay((50..150).random().toLong())

                // CSP: Lokaj!Chce()
                // Dostań zgodę lokaja
                butlerEnter.send(Sygnal.CHCE)

                // Skoro Lokaj pozwolił, bierzemy widelce
                // CSP: Widelec(i)!Chce() ...
                leftFork.send(Sygnal.CHCE)
                rightFork.send(Sygnal.CHCE)

                // CSP: je
                println("Filozof $me: JE POSIŁEK! (Mniam)")
                delay(200)

                // Odkladanie widelców
                rightFork.send(Sygnal.ODKLADA)
                leftFork.send(Sygnal.ODKLADA)

                // CSP: Lokaj!Wychodzę()
                // Zwalniamy miejsce u Lokaja
                butlerExit.send(Sygnal.WYCHODZE)
                println("Filozof $me: Skończył i wyszedł.")
            }
        }
    }

    // Pozwalamy symulacji działać przez 10 sekund
    delay(10000)
    println("Koniec czasu symulacji.")
    coroutineContext.cancelChildren()
}