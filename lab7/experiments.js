// Experiments script - runs multiple tests with different philosopher counts
var Fork = require('./app').Fork;
var Waiter = require('./app').Waiter;
var Philosopher = require('./app').Philosopher;
var fs = require('fs');

function runExperiment(numPhilosophers, meals, mode, callback) {
    var forks = [];
    var philosophers = [];
    var waiter = null;

    // Setup
    for (var i = 0; i < numPhilosophers; i++) {
        forks.push(new Fork());
    }

    for (var i = 0; i < numPhilosophers; i++) {
        philosophers.push(new Philosopher(i, forks));
    }

    // Start based on mode
    if (mode === 'asym') {
        for (var i = 0; i < numPhilosophers; i++) {
            philosophers[i].startAsym(meals);
        }
    } else if (mode === 'conductor') {
        waiter = new Waiter(numPhilosophers - 1);
        for (var i = 0; i < numPhilosophers; i++) {
            philosophers[i].startConductor(meals, waiter);
        }
    }

    // Wait for completion and collect results
    var timeout = (meals + 2) * 250;
    setTimeout(function () {
        var results = {
            mode: mode,
            numPhilosophers: numPhilosophers,
            meals: meals,
            philosophers: []
        };

        var totalWait = 0;
        for (var i = 0; i < numPhilosophers; i++) {
            var p = philosophers[i];
            totalWait += p.totalWaitTime;
            results.philosophers.push({
                id: i,
                mealsEaten: p.mealsEaten,
                totalWaitTime: p.totalWaitTime
            });
        }

        results.totalWaitTime = totalWait;
        results.avgWaitTime = totalWait / numPhilosophers;

        callback(results);
    }, timeout);
}

function runAllExperiments() {
    var philosopherCounts = [3, 5, 7, 10, 15, 20];
    var meals = 15;  // więcej posiłków = lepsze statystyki
    var modes = ['asym', 'conductor'];
    var allResults = [];

    console.log('Starting experiments...\n');

    var currentExperiment = 0;
    var totalExperiments = philosopherCounts.length * modes.length;

    function runNext() {
        if (currentExperiment >= totalExperiments) {
            // All done - save results
            console.log('\nAll experiments completed!');
            console.log('Saving results to results.json...');

            fs.writeFileSync('lab7/results.json', JSON.stringify(allResults, null, 2));
            console.log('Results saved!');

            // Print summary
            console.log('\n=== SUMMARY ===');
            allResults.forEach(function (r) {
                console.log(`${r.mode} with ${r.numPhilosophers} philosophers: avg wait ${r.avgWaitTime.toFixed(2)}ms`);
            });

            return;
        }

        var modeIdx = Math.floor(currentExperiment / philosopherCounts.length);
        var countIdx = currentExperiment % philosopherCounts.length;
        var mode = modes[modeIdx];
        var count = philosopherCounts[countIdx];

        console.log(`Running: ${mode} mode, ${count} philosophers...`);

        runExperiment(count, meals, mode, function (results) {
            allResults.push(results);
            console.log(`  -> Avg wait time: ${results.avgWaitTime.toFixed(2)}ms`);
            currentExperiment++;
            runNext();
        });
    }

    runNext();
}

// Run experiments
runAllExperiments();
