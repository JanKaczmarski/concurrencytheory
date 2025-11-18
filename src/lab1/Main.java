package lab1;

public class Main {
    /*
    Zadanie 5:
    - Min wartość rejestru na koniec to: 2
    - Max wartość rejestru na koniec to: 5*N
    Uzasadniene:
    1. Dla max jest proste - kiedy żadana operacja inkrementacji nie zostanie nadpisania (anulwoana) otrzymamy 5*N
    2. Dla min możemy zauważyć, że każdy wątek musi wykonać dokladnie 5 iteracji. Przeprowadze dowód poprzez pokazanie, że
    uzyskanie wartości 1 jest niemożliwe, a uzyskanie wartości 2 jest.
    Każdy wątek musi wykonać 5 iteracji. W 1 iteracji bierze minium 0 inkremetuje o 1 i zapisuje 1 w rejestrze. Teraz z rejestru nie
    można nigdy wziąć wartości 0 (jest już tam 1 a my musimy odczytać wartość z rejestru). Dlatego wartość 1 przy 5 iteracjach
    na proces jest niemożliwe.
    Uzyskanie wartości 2 jest możliwe i można to pokazać przez przyklad:
    3 wątki: A, B, C - lub więcej
              <!Tutaj A tylko zapisuje!>

    1. A: bierze 0 [0]
    2: B: bierze 0 [0]
    3: A: zapisuje 1 [1]
    4: A bierze i inkremetuje [2]
    5: A bierze i inkremetuje [3]
    6: A bierze i inkremetuje [4]
    NOTE!: A wykonało iterację 4 razy
    8: B inkrementuje do 1 [1]
    9: A bierze 1 <- WAŻNE Tutaj A bierze 1 bo B zapisało 1
    10: B inkrementuje [2]
    11: B inkrementuje [3]
    12: B inkrementuje [4]
    13: B inkrementuje [5]
    NOTE!: B wykonało iterację 5 razy
    14: C bierze i inkrementuje 5 [6]
    15: C bierze i inkrementuje 6 [7]
    ... Wszystkie pozostałe wątki oprócz A biorą i inkrementują wartość z rejestru
    NOTE!: Jako ostatnia iteracja wchodzi A i nadpisuje rejestr z wartością 2
    16: A zapisuje 1 + 1 = 2
    NOTE!: A wykonało iterację 5 razy

    Koniec: 2
     */
    public static void main(String[] args) {
        zad3();
    }

    private static void zad1_2() {
        Counter counter = new Counter();

        Thread firstThread = new Thread(() -> {
            System.out.println("First thread is running.");
            for (int i = 0; i < 1000000; i++) {
                counter.increment();
            }
        });

        Thread secondThread = new Thread(() -> {
            System.out.println("Second thread is running.");
            for (int i = 0; i < 1000000; i++) {
                counter.decrement();
            }
        });

        secondThread.start();
        firstThread.start();
        try {
            secondThread.join();
            firstThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("lab1.Counter: " + counter.counter);
    }

    private static void zad3() {
        int total = 200;
        Buffer buffer = new Buffer();

        Producer prod = new Producer(buffer, total);
        Consumer cons = new Consumer(buffer, total);

        Thread prodThread = new Thread(prod);
        Thread consThread = new Thread(cons);

        prodThread.start();
        consThread.start();
    }
}