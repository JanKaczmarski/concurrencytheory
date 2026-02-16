package lab10

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

/*
// Producent i konsumer oraz procesy przetwarzające
// N - liczba przetwarzaczy

[
  PRODUCER ::
    p: porcja;
    *[ true ->
         produkuj(p);
         PRZETWARZACZ(0)!p
     ]

  ||

  PRZETWARZACZ(i: 0..N-1) ::
    p: porcja;
    *[ true ->
         // Sekcja pobierania danych (Input)
         [ i = 0    -> PRODUCER?p
         [] i <> 0  -> PRZETWARZACZ(i-1)?p
         ];

         // Sekcja wysyłania danych (Output)
         [ i = N-1  -> CONSUMER!p
         [] i <> N-1 -> PRZETWARZACZ(i+1)!p
         ]
     ]

  ||

  CONSUMER ::
    p: porcja;
    *[ PRZETWARZACZ(N-1)?p ->
         konsumuj(p)
     ]
]
 */

const val N_PROCESSORS = 4

fun main() = runBlocking {
    println("--- Start Zadania 1: Taśma produkcyjna (Szeregowa) ---")

    val channels = List(N_PROCESSORS + 1) { Channel<Int>(Channel.RENDEZVOUS) }

    // Producer
    launch {
        var p = 1 // init value
        while (isActive) {
            println("PRODUCER: Wpuszczam surowy produkt $p na taśmę (kanał 0)")

            channels[0].send(p)
            p++
            delay(1000) // Produkuje co sekundę
        }
    }

    // Processors
    for (i in 0 until N_PROCESSORS) {
        launch {
            val inputChannel = channels[i]      // Biorę z lewej (od poprzednika)
            val outputChannel = channels[i + 1] // Oddaję w prawo (do następnego)

            for (p in inputChannel) {
                // Symulacja pracy: Każdy pracownik dodaje 10 do wartości produktu
                val processed = p + 10

                println("   PRZETWARZACZ($i): Dostałem $p -> Zrobiłem $processed -> Podaję dalej")
                delay(200)

                // Wyślij do następnego
                outputChannel.send(processed)
            }
        }
    }

    // Consumer
    launch {
        // Konsument bierze z ostatniego kanału
        val finalChannel = channels[N_PROCESSORS]

        for (p in finalChannel) {
            println("CONSUMER: Odebrałem gotowy produkt: $p")
            println("---")
        }
    }

    delay(5000) // Czas trwania symulacji
    coroutineContext.cancelChildren()
    println("Koniec symulacji.")
}