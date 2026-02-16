package lab11

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select

/*
[
  FILOZOF(i: 0..4) ::
    *[ true ->
         mysli;
         Widelec(i)!Chce();
         Widelec((i+1) mod 5)!Chce();

         je;

         Widelec((i+1) mod 5)!Odkłada();
         Widelec(i)!Odkłada()
     ]

  ||

  WIDELEC(i: 0..4) ::
    *[
       Filozof(i)?Chce() ->
         Filozof(i)?Odklada()

    [] Filozof((i+4) mod 5)?Chce() ->
         Filozof((i+4) mod 5)?Odklada()
     ]
]
*/

enum class Sygnal { CHCE, ODKLADA, WYCHODZE }

fun main() = runBlocking {
    println("=== ZADANIE 1: Pięciu Filozofów (Wersja z blokadą) ===")

    val N = 5

    // channels[idFilozofa][idWidelca]
    val channels = Array(N) { i -> Array(N) { Channel<Sygnal>(Channel.RENDEZVOUS) } }

    // Widelce
    for (i in 0 until N) {
        launch {
            val me = i
            // Widelec 'i' jest pomiędzy Filozofem(i) a Filozofem((i+4)%5)
            // czyli ma lewego i prawego sąsiada
            val leftNeighborId = i
            val rightNeighborId = (i + 4) % N

            // Kanały, na których ten konkretny widelec nasłuchuje
            val channelFromLeft = channels[leftNeighborId][me]
            val channelFromRight = channels[rightNeighborId][me]

            while (isActive) {
                select<Unit> {
                    // Opcja 1: Sąsiad z lewej chce widelca
                    channelFromLeft.onReceive { msg ->
                        if (msg == Sygnal.CHCE) {
                            // Widelec zajęty. Czekamy na ODKLADA
                            channelFromLeft.receive()
                        }
                    }
                    // Opcja 2: Sąsiad z prawej chce widelca
                    channelFromRight.onReceive { msg ->
                        if (msg == Sygnal.CHCE) {
                            // Widelec zajęty. Czekamy na ODKLADA.
                            channelFromRight.receive()
                        }
                    }
                }
            }
        }
    }

    // Uruchomienie Filozofów
    for (i in 0 until N) {
        launch {
            val me = i
            val leftForkId = i
            val rightForkId = (i + 1) % N

            val toLeftFork = channels[me][leftForkId]
            val toRightFork = channels[me][rightForkId]

            while (isActive) {
                // CSP: mysli
                println("Filozof $me: Myśli...")
                delay(100)

                // CSP: Widelec(i)!Chce()
                println("Filozof $me: Chce lewy widelec ($leftForkId)")
                toLeftFork.send(Sygnal.CHCE)
                println("Filozof $me: Ma lewy widelec ($leftForkId)")

                delay(100)

                // CSP: Widelec((i+1) mod 5)!Chce()
                println("Filozof $me: Chce prawy widelec ($rightForkId) [MOŻLIWA BLOKADA]")

                withTimeout(2000) {
                    try {
                        toRightFork.send(Sygnal.CHCE)
                        println("Filozof $me: Ma prawy widelec -> JE!")

                        // CSP: je
                        delay(300)

                        // Oddawanie widelców
                        toRightFork.send(Sygnal.ODKLADA)
                        toLeftFork.send(Sygnal.ODKLADA)
                        println("Filozof $me: Odłożył widelce.")
                    } catch (e: TimeoutCancellationException) {
                        println("!!! Filozof $me: UMARŁ Z GŁODU (Blokada na prawym widelcu) !!!")
                        throw e
                    }
                }
            }
        }
    }
}