#!/usr/bin/env python3
import json
import matplotlib.pyplot as plt
import numpy as np

# Load results
with open('lab7/results.json', 'r') as f:
    results = json.load(f)

# Organize data by mode
modes_data = {}
for result in results:
    mode = result['mode']
    if mode not in modes_data:
        modes_data[mode] = {
            'philosophers': [],
            'avg_wait_times': [],
            'total_wait_times': []
        }

    modes_data[mode]['philosophers'].append(result['numPhilosophers'])
    modes_data[mode]['avg_wait_times'].append(result['avgWaitTime'])
    modes_data[mode]['total_wait_times'].append(result['totalWaitTime'])

# Create figure with subplots
fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 6))

# Plot 1: Average wait time per philosopher
for mode, data in modes_data.items():
    ax1.plot(data['philosophers'], data['avg_wait_times'],
             marker='o', linewidth=2, label=mode.capitalize())

ax1.set_xlabel('Number of Philosophers', fontsize=12)
ax1.set_ylabel('Average Wait Time (ms)', fontsize=12)
ax1.set_title('Average Wait Time per Philosopher', fontsize=14, fontweight='bold')
ax1.legend()
ax1.grid(True, alpha=0.3)

# Plot 2: Total wait time (all philosophers)
for mode, data in modes_data.items():
    ax2.plot(data['philosophers'], data['total_wait_times'],
             marker='s', linewidth=2, label=mode.capitalize())

ax2.set_xlabel('Number of Philosophers', fontsize=12)
ax2.set_ylabel('Total Wait Time (ms)', fontsize=12)
ax2.set_title('Total Wait Time (All Philosophers)', fontsize=14, fontweight='bold')
ax2.legend()
ax2.grid(True, alpha=0.3)

plt.tight_layout()
plt.savefig('lab7/dining_philosophers_results.png', dpi=300, bbox_inches='tight')
print("Plot saved to lab7/dining_philosophers_results.png")
plt.show()

# Print detailed statistics
print("\n=== Detailed Statistics ===")
for mode, data in modes_data.items():
    print(f"\n{mode.upper()} mode:")
    for i, n in enumerate(data['philosophers']):
        print(f"  {n} philosophers: avg={data['avg_wait_times'][i]:.2f}ms, total={data['total_wait_times'][i]:.0f}ms")

# Create individual philosopher breakdown for one example
print("\n=== Individual Philosopher Breakdown (example: 5 philosophers) ===")
for result in results:
    if result['numPhilosophers'] == 5:
        print(f"\n{result['mode'].upper()} mode:")
        for p in result['philosophers']:
            print(f"  Philosopher {p['id']}: {p['mealsEaten']} meals, {p['totalWaitTime']}ms wait")
