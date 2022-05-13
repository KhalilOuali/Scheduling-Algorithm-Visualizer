import java.util.ArrayList;
import java.util.Scanner;

public class ConsoleApp {
	static Scheduler procSched = new Scheduler();

	// Console Input
	private static Scheduler.Job inputJob(Scanner input) {
		String _id;
		int _timeOfArrival;
		int _estimatedCPUTime;
		int _priority;

		do {
			System.out.print("	Process ID: ");
			input.nextLine();
			_id = input.nextLine();
		} while (_id.isEmpty());

		do {
			System.out.print("	Arrival   : ");
			_timeOfArrival = input.nextInt();
		} while (_timeOfArrival < 0);

		do {
			System.out.print("	CPU Time  : ");
			_estimatedCPUTime = input.nextInt();
		} while (_estimatedCPUTime < 1);

		System.out.print("	Priority  : ");
		_priority = input.nextInt();

		return procSched.new Job(_id, _timeOfArrival, _estimatedCPUTime, _priority, 0);
	}

	private static void inputQueue(Scanner input) {
		ArrayList<Scheduler.Job> futureJobs = new ArrayList<>();

		int n;
		do {
			System.out.print("Number of Jobs: ");
			n = input.nextInt();
		} while (n < 1);

		while (futureJobs.size() < n) {
			System.out.println("- Job " + (futureJobs.size() + 1) + ":");
			futureJobs.add(inputJob(input));
		}

		System.out.println();

		procSched.configureQueue(futureJobs);
	}

	private static void inputAlgorithm(Scanner input) {
		boolean preemptive;
		byte[] criteria = { Scheduler.NONE, Scheduler.NONE, Scheduler.NONE };
		int quantum;

		byte c;
		do {
			System.out.println("0: FCFS | 1: SJF | 2: Priority | 3: Round Robin");
			System.out.print("Scheduling algorithm: ");
			c = input.nextByte();
		} while (c < 0 || 3 < c);

		if (c == 0 || c == 3)
			preemptive = false;
		else {
			System.out.print("(true | false) Preemptive? ");
			preemptive = input.nextBoolean();
		}

		if (c == 3) {
			do {
				System.out.print("Quantum: ");
				quantum = input.nextInt();
			} while (quantum < 1);

			procSched.configureAlgorithmRR(quantum);
		} else {
			criteria[0] = c;
			System.out.println("0: FCFS | 1: SJF | 2: Priority | -1: None");

			do {
				System.out.print("Second criterion: ");
				criteria[1] = input.nextByte();
			} while (criteria[1] < -1 || 2 < criteria[1] || criteria[1] == criteria[0]);

			if (criteria[1] != -1) {
				do {
					System.out.print("Third criterion: ");
					criteria[2] = input.nextByte();
				} while (criteria[2] < -1 || 2 < criteria[2] || criteria[2] == criteria[0] || criteria[2] == criteria[1]);
			}

			procSched.configureAlgorithm(preemptive, criteria);
		}

	}

	// Console Output
	private static void outputSchedule(ArrayList<Scheduler.Work> works) {
		for (Scheduler.Work w : works) {
			String activeJobId = (w.job == null) ? "Nothing" : w.job.id;
			System.out.printf("%2d - %2d: %s\n", w.from, w.to, activeJobId);
		}
	}

	private static void outputFinished() {
		for (Scheduler.Job j : procSched.finishedJobs) {
			System.out.println(
				"- Job ID: " + j.id + "\n" +
					"	Arrived     : " + j.properties[Scheduler._ARRIVAL] + "\n" +
					"	Finished    : " + j.finished + "\n" +
					"	Wait time   : " + j.waitTime() + "\n" +
					"	Turnaround time: " + j.turnAroundTime()
			);
		}
	}
 
	public static void main(String[] args) {
		try (Scanner input = new Scanner(System.in)) {
			inputQueue(input);
			inputAlgorithm(input);

			System.out.println("\n---------------- CPU Schedule:");
			ArrayList<Scheduler.Work> works = procSched.schedule();
			outputSchedule(works);

			boolean showJobs;
			System.out.print("\n(true | false) Show final jobs' state? ");
			showJobs = input.nextBoolean();
			if (showJobs) {
				System.out.println("\n---------------- Final results:");
				outputFinished();
			}

			System.out.println("\n---------------- Performance:");
			System.out.printf("Average turnaround time: %.2f\n", procSched.averageTurnAroundTime());
			System.out.printf("Average wait time:  %.2f\n", procSched.averageWaitTime());
		}

		System.out.println();
	}
}
