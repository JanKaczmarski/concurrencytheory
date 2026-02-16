
var Fork = function () {
    this.state = 0;
    return this;
}

Fork.prototype.acquire = function (cb, delay, totalWait) {
    // Binary Exponential Backoff
    var self = this;
    var maxDelay = 128;
    delay = delay || 1;
    totalWait = totalWait || 0;

    setTimeout(function () {
        if (self.state === 0) {
            // widelec wolny - zajmujemy go
            self.state = 1;
            cb(totalWait + delay);  // zwracamy łączny czas oczekiwania
        } else {
            // widelec zajęty - próbujemy ponownie z 2x dłuższym opóźnieniem
            var nextDelay = Math.min(delay * 2, maxDelay);
            self.acquire(cb, nextDelay, totalWait + delay);
        }
    }, delay);
}

Fork.prototype.release = function () {
    this.state = 0;
}

var Waiter = function (capacity) {
    this.capacity = capacity;
    this.current = 0;
    return this;
}

Waiter.prototype.acquire = function (cb, delay, totalWait) {
    var self = this;
    var maxDelay = 128;
    delay = delay || 1;
    totalWait = totalWait || 0;

    setTimeout(function () {
        if (self.current < self.capacity) {
            self.current++;
            cb(totalWait + delay);
        } else {
            var nextDelay = Math.min(delay * 2, maxDelay);
            self.acquire(cb, nextDelay, totalWait + delay);
        }
    }, delay);
}

Waiter.prototype.release = function () {
    this.current--;
}

var Philosopher = function (id, forks) {
    this.id = id;
    this.forks = forks;
    this.f1 = id % forks.length;
    this.f2 = (id + 1) % forks.length;
    this.totalWaitTime = 0;
    this.mealsEaten = 0;
    return this;
}

Philosopher.prototype.startNaive = function (count) {
    var forks = this.forks,
        f1 = this.f1,
        f2 = this.f2,
        id = this.id,
        self = this;

    // Naiwne rozwiązanie - każdy podnosi lewy, potem prawy
    var eat = function (n) {
        if (n >= count) {
            console.log(`Philosopher ${id}: FINISHED - ate ${self.mealsEaten} meals, total wait: ${self.totalWaitTime}ms`);
            return;
        }

        // Losowy czas myślenia (100-200ms)
        var thinkTime = 100 + Math.random() * 100;
        setTimeout(function () {
            forks[f1].acquire(function (waitTime1) {
                self.totalWaitTime += waitTime1;
                forks[f2].acquire(function (waitTime2) {
                    self.totalWaitTime += waitTime2;
                    console.log(`Philosopher ${id}: EATING meal #${n + 1}`);
                    self.mealsEaten++;
                    // Czas jedzenia
                    setTimeout(function () {
                        forks[f1].release();
                        forks[f2].release();
                        eat(n + 1);
                    }, 50);
                });
            });
        }, thinkTime);
    };

    eat(0);
}

Philosopher.prototype.startAsym = function (count) {
    var forks = this.forks,
        f1 = this.f1,
        f2 = this.f2,
        id = this.id,
        self = this;

    // Asymetryczne rozwiązanie - parzyści biorą najpierw prawy, nieparzyści lewy
    var eat = function (n) {
        if (n >= count) {
            console.log(`Philosopher ${id}: FINISHED - ate ${self.mealsEaten} meals, total wait: ${self.totalWaitTime}ms`);
            return;
        }

        // Czas myslenia 100 - 200 ms
        var thinkTime = 100 + Math.random() * 100;
        setTimeout(function () {
            // Asymetria: parzyści najpierw prawy (f2), nieparzyści najpierw lewy (f1)
            var first = (id % 2 === 0) ? f2 : f1;
            var second = (id % 2 === 0) ? f1 : f2;

            forks[first].acquire(function (waitTime1) {
                self.totalWaitTime += waitTime1;
                forks[second].acquire(function (waitTime2) {
                    self.totalWaitTime += waitTime2;
                    console.log(`Philosopher ${id}: EATING meal #${n + 1}`);
                    self.mealsEaten++;
                    // Czas jedzenia
                    setTimeout(function () {
                        forks[first].release();
                        forks[second].release();
                        eat(n + 1);
                    }, 50);
                });
            });
        }, thinkTime);
    };

    eat(0);
}

Philosopher.prototype.startConductor = function (count, waiter) {
    var forks = this.forks,
        f1 = this.f1,
        f2 = this.f2,
        id = this.id,
        self = this;

    // Rozwiązanie z lokajem - max N-1 filozofów może być przy stole
    var eat = function (n) {
        if (n >= count) {
            console.log(`Philosopher ${id}: FINISHED - ate ${self.mealsEaten} meals, total wait: ${self.totalWaitTime}ms`);
            return;
        }

        // Czas myślenia (100-200ms)
        var thinkTime = 100 + Math.random() * 100;
        setTimeout(function () {
            waiter.acquire(function (waiterWaitTime) {
                self.totalWaitTime += waiterWaitTime;
                forks[f1].acquire(function (waitTime1) {
                    self.totalWaitTime += waitTime1;
                    forks[f2].acquire(function (waitTime2) {
                        self.totalWaitTime += waitTime2;
                        console.log(`Philosopher ${id}: EATING meal #${n + 1}`);
                        self.mealsEaten++;
                        // Czas jedzenia
                        setTimeout(function () {
                            forks[f1].release();
                            forks[f2].release();
                            waiter.release();
                            // Rekursywne wywołanie dla następnego posiłku
                            eat(n + 1);
                        }, 50);
                    });
                });
            });
        }, thinkTime);
    };

    eat(0);
}


// Main execution
if (require.main === module) {
    var N = 5;
    var meals = 10;
    var mode = process.argv[2] || 'naive';  // naive, asym, conductor

    var forks = [];
    var philosophers = [];

    console.log(`\n=== Starting Dining Philosophers - ${mode} mode ===`);
    console.log(`Philosophers: ${N}, Meals per philosopher: ${meals}\n`);

    for (var i = 0; i < N; i++) {
        forks.push(new Fork());
    }

    for (var i = 0; i < N; i++) {
        philosophers.push(new Philosopher(i, forks));
    }

    if (mode === 'naive') {
        console.log('WARNING: Naive solution may deadlock!\n');
        for (var i = 0; i < N; i++) {
            philosophers[i].startNaive(meals);
        }
    } else if (mode === 'asym') {
        for (var i = 0; i < N; i++) {
            philosophers[i].startAsym(meals);
        }
    } else if (mode === 'conductor') {
        var waiter = new Waiter(N - 1);  // N-1 philosophers at table
        for (var i = 0; i < N; i++) {
            philosophers[i].startConductor(meals, waiter);
        }
    }

    // Print summary after some time
    setTimeout(function () {
        console.log('\n=== SUMMARY ===');
        var totalWait = 0;
        for (var i = 0; i < N; i++) {
            var p = philosophers[i];
            totalWait += p.totalWaitTime;
            console.log(`Philosopher ${i}: ${p.mealsEaten} meals, ${p.totalWaitTime}ms wait time`);
        }
        console.log(`\nTotal wait time across all philosophers: ${totalWait}ms`);
        console.log(`Average wait time per philosopher: ${(totalWait / N).toFixed(2)}ms`);
    }, (meals + 2) * 250);  // Enough time for all meals
}

// Export for experiments
module.exports = {
    Fork: Fork,
    Waiter: Waiter,
    Philosopher: Philosopher
};