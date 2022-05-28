import java.util.ArrayList;
import java.util.Scanner;

public class ConsoleApp {
	static Scheduler procSched = new Scheduler();

	// Input verficiation
	private static int readInt(Scanner input) {
		while (!input.hasNextInt()) {
			input.next();
			System.out.println("Input error: please enter an integer number.");
		}

		return input.nextInt();
	}

	private static boolean readBoolean(Scanner input) {
		while (!input.hasNextBoolean()) {
			input.next();
			System.out.println("Input error: please enter an \"true\" or \"false\".");
		}

		return input.nextBoolean();
	}

	private static byte readByte(Scanner input) {
		while (!input.hasNextByte()) {
			input.next();
			System.out.println("Input error: please enter an integer number.");
		}

		return input.nextByte();
	}

	// Console Input
	private static Scheduler.Job inputJob(Scanner input) {
		String _id;
		int _timeOfArrival;
		int _estimatedCPUTime;
		int _priority;

		System.out.print("	Process ID: ");
		_id = input.next();
		//System.out.println("Input error: please enter a valid process id.");

		while (true) {
			System.out.print("	Arrival   : ");
			_timeOfArrival = readInt(input);

			if (_timeOfArrival < 0)
				System.out.println("Input error: time of arrival must be >= 0.");
			else
				break;
		}

		while (true) {
			System.out.print("	CPU Time  : ");
			_estimatedCPUTime = readInt(input);

			if (_estimatedCPUTime < 1)
				System.out.println("Input error: estimated CPU time must be > 0.");
			else
				break;
		}

		System.out.print("	Priority  : ");
		_priority = readInt(input);

		return procSched.new Job(_id, _timeOfArrival, _estimatedCPUTime, _priority, 0);
	}

	private static void inputQueue(Scanner input) {
		ArrayList<Scheduler.Job> futureJobs = new ArrayList<>();

		int n;
		while (true) {
			System.out.print("Number of Jobs: ");
			n = readInt(input);

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
		boolean preemptive = false;
		byte[] criteria = { Scheduler.NONE, Scheduler.NONE, Scheduler.NONE };
		int quantum;

		byte c;
		while (true) {
			System.out.println("0: FCFS | 1: SJF | 2: Priority | 3: Round Robin");
			System.out.print("Scheduling algorithm: ");
			c = readByte(input);

			if (c < 0 || 3 < c)
				System.out.println("Input error: please choose one of the indicated options.");
			else
				break;
		}

		if (c == 1 || c == 2) {
			System.out.print("(true | false) Preemptive? ");
			preemptive = readBoolean(input);
		}

		if (c == 3) {
			while (true) {
				System.out.print("Quantum: ");
				quantum = readInt(input);

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
				criteria[1] = readByte(input);

				if (criteria[1] < -1 || 2 < criteria[1])
					System.out.println("Input error: please choose one of the indicated options.");
				else if (criteria[1] == criteria[0])
					System.out.println("Input error: Selection criteria must be different.");
				else
					break;
			}

			if (criteria[1] != -1) {
				while (true) {
					System.out.print("Third criterion: ");
					criteria[2] = readByte(input);

					if (criteria[2] < -1 || 2 < criteria[2])
						System.out.println("Input error: please choose one of the indicated options.");
					else if (criteria[2] == criteria[0] || criteria[2] == criteria[1])
						System.out.println("Input error: Selection criteria must be different.");
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
			String activeJobId = "Nothing";

			if (w.job != null)
				activeJobId = w.job.id + (w.to == w.job.finished ? " (Finished)" : " (Interrupted)");
			
			System.out.printf("%2d - %2d: %s\n", w.from, w.to, activeJobId);
		}
	}

	private static void outputFinished() {
		for (Scheduler.Job j : procSched.finishedJobs) {
			System.out.println(
				"- Job ID: " + j.id + "\n" + 
				"	Arrived at     : " + j.properties[Scheduler._ARRIVAL] + "\n" +
				"	Finished at    : " + j.finished + "\n" +
				"	Waiting time   : " + j.waitTime() + "\n" +
				"	Turnaround time: " + j.turnAroundTime()
			);
		}
	}

	// Main
	public static void main(String[] args) {
		try (Scanner input = new Scanner(System.in)) {
			inputQueue(input);
			inputAlgorithm(input);

			System.out.println("\n---------------- CPU Schedule:");
			ArrayList<Scheduler.Work> works = procSched.schedule();
			outputSchedule(works);

			System.out.println("\n---------------- Performance:");
			System.out.printf("Average turnaround time: %.2f\n", procSched.averageTurnAroundTime());
			System.out.printf("Average waiting time   :  %.2f\n", procSched.averageWaitTime());

			boolean showJobs;
			System.out.print("\n(true | false) Show final jobs' states? ");
			showJobs = readBoolean(input);
			if (showJobs) {
				System.out.println("\n---------------- Final results:");
				outputFinished();
			}

			input.nextLine();
			System.out.println("\nPress enter to exit.");
			input.nextLine();
		} catch (Exception e) {
			System.out.println("I/O error. Program will exit.");
		}

		System.out.println();
	}
}
