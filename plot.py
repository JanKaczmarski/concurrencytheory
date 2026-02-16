import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns

# Set style
sns.set_style("whitegrid")


def analyze_logs(starving_file, fair_file, title_suffix=""):
    """Analyze and plot comparison between naive and fair approaches"""

    # Read CSV files
    df_starving = pd.read_csv(starving_file)
    df_fair = pd.read_csv(fair_file)

    # Convert nanoseconds to milliseconds
    df_starving["WaitTimeMs"] = df_starving["WaitTimeNanos"] / 1_000_000
    df_fair["WaitTimeMs"] = df_fair["WaitTimeNanos"] / 1_000_000

    # Create figure with 2 subplots
    fig, axes = plt.subplots(1, 2, figsize=(14, 6))
    fig.suptitle(f"Producer-Consumer Wait Time Analysis {title_suffix}", fontsize=16, fontweight="bold")

    # 1. Histogram comparison with mean Amount per bin
    ax1 = axes[0]
    bins = 50

    # Plot histograms (frequency of WaitTime)
    counts_starving, bin_edges, patches_starving = ax1.hist(
        df_starving["WaitTimeMs"],
        bins=bins,
        alpha=0.3,
        label="Naive - Frequency",
        color="red",
        edgecolor="black",
    )
    counts_fair, _, patches_fair = ax1.hist(
        df_fair["WaitTimeMs"],
        bins=bin_edges,  # Use same bins
        alpha=0.3,
        label="Fair - Frequency",
        color="blue",
        edgecolor="black"
    )

    # Calculate mean Amount for each bin
    bin_centers = (bin_edges[:-1] + bin_edges[1:]) / 2

    # For starving approach
    mean_amounts_starving = []
    for i in range(len(bin_edges) - 1):
        mask = (df_starving["WaitTimeMs"] >= bin_edges[i]) & (df_starving["WaitTimeMs"] < bin_edges[i+1])
        bin_data = df_starving[mask]
        if len(bin_data) > 0:
            mean_amounts_starving.append(bin_data["Amount"].mean())
        else:
            mean_amounts_starving.append(0)

    # For fair approach
    mean_amounts_fair = []
    for i in range(len(bin_edges) - 1):
        mask = (df_fair["WaitTimeMs"] >= bin_edges[i]) & (df_fair["WaitTimeMs"] < bin_edges[i+1])
        bin_data = df_fair[mask]
        if len(bin_data) > 0:
            mean_amounts_fair.append(bin_data["Amount"].mean())
        else:
            mean_amounts_fair.append(0)

    # Plot mean Amount on secondary y-axis
    ax1_secondary = ax1.twinx()
    ax1_secondary.plot(
        bin_centers,
        mean_amounts_starving,
        color="darkred",
        linewidth=2.5,
        marker='o',
        markersize=3,
        label="Naive - Mean Amount",
        alpha=0.8
    )
    ax1_secondary.plot(
        bin_centers,
        mean_amounts_fair,
        color="darkblue",
        linewidth=2.5,
        marker='s',
        markersize=3,
        label="Fair - Mean Amount",
        alpha=0.8
    )

    ax1.set_xlabel("Wait Time (ms)", fontsize=12)
    ax1.set_ylabel("Frequency (count)", fontsize=12)
    ax1_secondary.set_ylabel("Mean Amount", fontsize=12)
    ax1.set_title("Wait Time Distribution with Mean Amount", fontsize=14, fontweight="bold")
    ax1.set_yscale("log")

    # Combine legends from both axes
    lines1, labels1 = ax1.get_legend_handles_labels()
    lines2, labels2 = ax1_secondary.get_legend_handles_labels()
    ax1.legend(lines1 + lines2, labels1 + labels2, fontsize=9, loc='upper right')

    ax1.grid(alpha=0.3)

    # 2. Statistics table
    ax2 = axes[1]
    stats_data = []

    for approach, df in [("Naive", df_starving), ("Fair", df_fair)]:
        for type_name in ["Producer", "Consumer"]:
            subset = df[df["Type"] == type_name]
            stats_data.append(
                [
                    f"{approach}\n{type_name}",
                    f"{subset['WaitTimeMs'].mean():.2f}",
                    f"{subset['WaitTimeMs'].median():.2f}",
                    f"{subset['WaitTimeMs'].max():.2f}",
                    f"{subset['WaitTimeMs'].std():.2f}",
                ]
            )

    ax2.axis("tight")
    ax2.axis("off")
    table = ax2.table(
        cellText=stats_data,
        colLabels=["Type", "Mean (ms)", "Median (ms)", "Max (ms)", "Std Dev (ms)"],
        cellLoc="center",
        loc="center",
        bbox=[0, 0, 1, 1],
    )
    table.auto_set_font_size(False)
    table.set_fontsize(10)
    table.scale(1, 2.5)

    # Style the header
    for i in range(5):
        table[(0, i)].set_facecolor("#40466e")
        table[(0, i)].set_text_props(weight="bold", color="white")

    # Alternate row colors
    for i in range(1, len(stats_data) + 1):
        for j in range(5):
            if i % 2 == 0:
                table[(i, j)].set_facecolor("#f0f0f0")

    ax2.set_title("Statistics Summary", fontsize=14, fontweight="bold", pad=20)

    plt.tight_layout()
    output_file = starving_file.replace(".csv", "_analysis.png")
    plt.savefig(output_file, dpi=300, bbox_inches="tight")
    print(f"✓ Plot saved as '{output_file}'")

    # Print console statistics
    print("\n" + "=" * 70)
    print(f"STATISTICS SUMMARY {title_suffix}")
    print("=" * 70)

    print("\nNAIVE APPROACH:")
    print(df_starving.groupby("Type")["WaitTimeMs"].describe())

    print("\nFAIR APPROACH:")
    print(df_fair.groupby("Type")["WaitTimeMs"].describe())

    # Comparison metrics
    print("\n" + "=" * 70)
    print("COMPARISON METRICS")
    print("=" * 70)

    max_naive = df_starving["WaitTimeMs"].max()
    max_fair = df_fair["WaitTimeMs"].max()
    print(f"\nMaximum wait time:")
    print(f"  Naive: {max_naive:.2f} ms")
    print(f"  Fair: {max_fair:.2f} ms")
    if max_naive > max_fair:
        print(f"  Improvement: {(1 - max_fair/max_naive)*100:.1f}%")
    else:
        print(f"  Difference: {(max_fair/max_naive - 1)*100:.1f}% worse")

    mean_naive = df_starving["WaitTimeMs"].mean()
    mean_fair = df_fair["WaitTimeMs"].mean()
    print(f"\nAverage wait time:")
    print(f"  Naive: {mean_naive:.2f} ms")
    print(f"  Fair: {mean_fair:.2f} ms")
    if mean_naive > mean_fair:
        print(f"  Improvement: {(1 - mean_fair/mean_naive)*100:.1f}%")
    else:
        print(f"  Difference: {(mean_fair/mean_naive - 1)*100:.1f}% worse")


if __name__ == "__main__":
    try:
        # Analyze different test configurations
        configs = [
            ("log_starving.csv", "log_fair.csv"),
        ]

        for starving, fair in configs:
            try:
                print(f"\n{'='*70}")
                print(f"Analyzing files: {starving} and {fair}")
                print("=" * 70)
                analyze_logs(starving, fair)
                plt.show()
            except FileNotFoundError:
                print("One of the log files is missing. Skipping this configuration.")
                continue

    except Exception as e:
        print(f"Error: {e}")
        import traceback

        traceback.print_exc()
