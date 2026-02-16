package lab10

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.selectUnbiased

/*
// Producent i konsument z wybranym pośrednikiem (pula buforów)
// N - liczba pośredników

[
  PRODUCER ::
    p: porcja;
    *[ true ->
         produkuj(p);
         // Oczekiwanie, aż którykolwiek pośrednik zgłosi gotowość (sygnał JESZCZE)
         [ (i:0..N-1) POSREDNIK(i)?JESZCZE() ->
             POSREDNIK(i)!p
         ]
     ]

  ||

  POSREDNIK(i:0..N-1) ::
    p: porcja;
    *[ true ->
         // Zgłoszenie gotowości do producenta
         PRODUCER!JESZCZE();
         // Odbiór danych i natychmiastowe przekazanie do konsumenta
         [ PRODUCER?p ->
             CONSUMER!p
         ]
     ]

  ||

  CONSUMER ::
    p: porcja;
    *[ (i:0..N-1) POSREDNIK(i)?p ->
         konsumuj(p)
     ]
]

 */

const val N_BROKERS = 3

fun main() = runBlocking {
    println("--- Start Zadania 2: Pośrednicy z select ---")

    // Kanały Producent -> Pośrednicy
    val prodToBrokerChannels = List(N_BROKERS) { Channel<Int>(Channel.RENDEZVOUS) }
    // Kanały Pośrednicy -> Konsument
    val brokerToConsChannels = List(N_BROKERS) { Channel<Int>(Channel.RENDEZVOUS) }

    // Producer
    launch {
        var p = 1
        while (isActive) {
            delay(400) // Symulacja czasu produkcji
            println("PRODUCER: Chcę wysłać $p")

            // Take one free chanel that can accept data
            selectUnbiased<Unit> {
                prodToBrokerChannels.forEachIndexed { i, channel ->
                    // jesli i jest wolne wyslij tam p
                    channel.onSend(p) {
                        println("PRODUCER: Wysłano $p do Pośrednika $i")
                    }
                }
            }
            p++
        }
    }

    // Broker
    for (i in 0 until N_BROKERS) {
        launch {
            // Pośrednik działa w pętli: odbiera od producenta, wysyła do konsumenta
            for (p in prodToBrokerChannels[i]) {
                println("   POSREDNIK($i): Odebrałem $p, przekazuję dalej...")
                delay((100..500).random().toLong()) // Symulacja pracy pośrednika
                brokerToConsChannels[i].send(p)
            }
        }
    }

    // Consumer
    launch {
        while (isActive) {
            // czeka na dowolne dostepne dane od któregokolwiek pośrednika
            val receivedVal = selectUnbiased<Int> {
                brokerToConsChannels.forEachIndexed { i, channel ->
                    // Jeśli na kanale 'i' są dane, odbierz je
                    channel.onReceive { p ->
                        println("CONSUMER: Odebrałem $p od Pośrednika $i")
                        p // zwróć odebraną wartość
                    }
                }
            }
        }
    }

    delay(4000)
    coroutineContext.cancelChildren()
    println("Koniec symulacji.")
}