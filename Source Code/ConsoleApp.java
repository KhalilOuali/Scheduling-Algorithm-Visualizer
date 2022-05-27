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

		while (true) {
			System.out.print("	Process ID: ");

			_id = input.next();

			if (_id.isEmpty())
				System.out.println("Input error: please enter a valid process id.");
			else
				break;
		}

		while (true) {
			System.out.print("	Arrival   : ");
			_timeOfArrival = input.nextInt();

			if (_timeOfArrival < 0)
				System.out.println("Input error: time of arrival must be ≥ 0.");
			else
				break;
		}
		;

		while (true) {
			System.out.print("	CPU Time  : ");
			_estimatedCPUTime = input.nextInt();

			if (_estimatedCPUTime < 1)
				System.out.println("Input error: estimated CPU time must be > 0.");
			else
				break;
		}

		System.out.print("	Priority  : ");
		_priority = input.nextInt();

		return procSched.new Job(_id, _timeOfArrival, _estimatedCPUTime, _priority, 0);
	}

	private static void inputQueue(Scanner input) {
		ArrayList<Scheduler.Job> futureJobs = new ArrayList<>();

		int n;
		while (true) {
			System.out.print("Number of Jobs: ");
			n = input.nextInt();

			if (n < 1)
				System.out.println("Input error: Number of jobs must be > 0.");
			else
				break;
		}

		int i = 0;
		int lastArrivalTime = 0;
		while (i < n) {
			System.out.println("- Job " + (i + 1) + ":");
			Scheduler.Job job = inputJob(input);

			if (job.properties[Scheduler._ARRIVAL] < lastArrivalTime)
				System.out.println("Input error: arrival times must be in ascending order.");
			else {
				futureJobs.add(job);
				i++;
				lastArrivalTime = job.properties[Scheduler._ARRIVAL];
			}
		}

		System.out.println();

		procSched.configureQueue(futureJobs);
	}

	private static void inputAlgorithm(Scanner input) {
		boolean preemptive;
		byte[] criteria = { Scheduler.NONE, Scheduler.NONE, Scheduler.NONE };
		int quantum;

		byte c;
		while (true) {
			System.out.println("0: FCFS | 1: SJF | 2: Priority | 3: Round Robin");
			System.out.print("Scheduling algorithm: ");
			c = input.nextByte();

			if (c < 0 || 3 < c)
				System.out.println("Input error: please choose one of the indicated options.");
			else
				break;
		}

		if (c == 0 || c == 3)
			preemptive = false;
		else {
			System.out.print("(true | false) Preemptive? ");
			preemptive = input.nextBoolean();
		}

		if (c == 3) {
			while (true) {
				System.out.print("Quantum: ");
				quantum = input.nextInt();

				if (quantum < 1)
					System.out.println("Input error: quantum must be > 0.");
				else
					break;
			}

			procSched.configureAlgorithmRR(quantum);
		} else {
			criteria[0] = c;
			System.out.println("0: FCFS | 1: SJF | 2: Priority | -1: None");

			while (true) {
				System.out.print("Second criterion: ");
				criteria[1] = input.nextByte();

				if (criteria[1] < -1 || 2 < criteria[1])
					System.out.println("Input error: please choose one of the indicated options.");
				else if (criteria[1] == criteria[0])
					System.out.println("Input error: Scheduling criteria must be different.");
				else
					break;
			}

			if (criteria[1] != -1) {
				while (true) {
					System.out.print("Third criterion: ");
					criteria[2] = input.nextByte();

					if (criteria[2] < -1 || 2 < criteria[2])
						System.out.println("Input error: please choose one of the indicated options.");
					else if (criteria[2] == criteria[0] || criteria[2] == criteria[1])
						System.out.println("Input error: Scheduling criteria must be different.");
					else
						break;
				}
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
			System.out.println("- Job ID: " + j.id + "\n" + "	Arrived     : " + j.properties[Scheduler._ARRIVAL] + "\n" + "	Finished    : " + j.finished + "\n" + "	Wait time   : " + j.waitTime()
					+ "\n" + "	Turnaround time: " + j.turnAroundTime());
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
			System.out.print("\n(true | false) Show final jobs' states? ");
			showJobs = input.nextBoolean();
			if (showJobs) {
				System.out.println("\n---------------- Final results:");
				outputFinished();
			}

			System.out.println("\n---------------- Performance:");
			System.out.printf("Average turnaround time: %.2f\n", procSched.averageTurnAroundTime());
			System.out.printf("Average wait time:  %.2f\n", procSched.averageWaitTime());

			input.nextLine();
			System.out.println("\nPress enter to exit.");
			input.nextLine();
		}

		System.out.println();
	}
}
