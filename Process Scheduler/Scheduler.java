import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Scheduler {
	// Global Constants
	private static final byte _NONE = -1;
	private static final byte _ARRIVAL = 0;
	private static final byte _TIMELEFT = 1;
	private static final byte _PRIORITY = 2;

	public static final byte NONE = -1;
	public static final byte FCFS = 0;
	public static final byte SJF = 1;
	public static final byte PRIORITY = 2;
	public static final byte ROUNDROBIN = 3;

	// JOB CLASS: processes
	public class Job implements Comparable<Job> {
		String id;
		private int[] properties = new int[3];
		private int cpuTime;
		public int finished;
		public int order;

		// Constructor
		Job(Scanner input) {
			do {
				System.out.print("	Process ID: ");
				input.nextLine();
				id = input.nextLine();
			} while (id.isEmpty());

			do {
				System.out.print("	Arrival   : ");
				properties[_ARRIVAL] = input.nextInt();
			} while (properties[_ARRIVAL] < 0);

			do {
				System.out.print("	CPU Time  : ");
				properties[_TIMELEFT] = input.nextInt();
				cpuTime = properties[_TIMELEFT];
			} while (cpuTime < 1);

			System.out.print("	Priority  : ");
			properties[_PRIORITY] = input.nextInt();

			finished = _NONE;
		}

		Job(String _id, int _timeOfArrival, int _estimatedCPUTime, int _priority) {
			id = _id;
			properties[_ARRIVAL] = _timeOfArrival;
			properties[_TIMELEFT] = _estimatedCPUTime;
			cpuTime = _estimatedCPUTime;
			properties[_PRIORITY] = _priority;
			finished = _NONE;
		}

		Job(String _id, int _timeOfArrival, int _estimatedCPUTime, int _priority, int _order) {
			id = _id;
			properties[_ARRIVAL] = _timeOfArrival;
			properties[_TIMELEFT] = _estimatedCPUTime;
			cpuTime = _estimatedCPUTime;
			properties[_PRIORITY] = _priority;
			finished = _NONE;
			order = _order;
		}

		// Performance criteria
		public int serviceTime() {
			return finished - properties[_ARRIVAL];
		}

		public int waitTime() {
			return finished - properties[_ARRIVAL] - cpuTime;
		}

		@Override
		public int compareTo(Scheduler.Job o) {
			for (int i = 0; i < 3; i++) {
				if (criteria[i] != _NONE) {
					if (properties[criteria[i]] < o.properties[criteria[i]])
						return -1;

					if (properties[criteria[i]] > o.properties[criteria[i]])
						return 1;
				}
			}
			return 0;
		}
	}

	// WORK CLASS: final printed schedule
	public class Work {
		int from, to;
		Job job;

		Work() {
			from = currentTime;
			to = nextTime;
			job = activeJob;
		}
	}

	// Algorithm parameters
	private boolean roundRobin = false;
	private boolean preemptive = false;
	private byte[] criteria = { FCFS, NONE, NONE };
	private int quantum = 2;

	// Scheduling Variables
	private int currentTime, nextTime;
	private Job activeJob = null;

	// Queue Variables
	private ArrayList<Job> finishedJobs;
	private ArrayList<Job> presentJobs;
	private ArrayList<Job> futureJobs;
	private ArrayList<Work> finalSchedule;

	// Scheduling
	private void updateJobs() {
		int i = 0;
		while (i < futureJobs.size()) {
			if (futureJobs.get(i).properties[_ARRIVAL] <= currentTime) {
				presentJobs.add(futureJobs.get(i));
				futureJobs.remove(i);
			} else
				i++;
		}
	}

	private void chooseJob() {
		if (activeJob != null)
			presentJobs.add(activeJob);

		if (presentJobs.isEmpty())
			activeJob = null;
		else {
			activeJob = roundRobin ? presentJobs.get(0) : Collections.min(presentJobs);
			presentJobs.remove(activeJob);
		}
	}

	private void nextEvent() {
		ArrayList<Integer> possibleEvents = new ArrayList<>();

		if (activeJob != null) {
			possibleEvents.add(currentTime + activeJob.properties[_TIMELEFT]);

			if (roundRobin)
				possibleEvents.add(currentTime + quantum);
		}

		if ((!futureJobs.isEmpty()) && (activeJob == null || (preemptive && !roundRobin)))
			possibleEvents.add(futureJobs.get(0).properties[_ARRIVAL]);

		nextTime = possibleEvents.isEmpty() ? _NONE : Collections.min(possibleEvents);
	}

	private void logWork() {
		finalSchedule.add(new Work());
	}

	private void executeJob() {
		if (activeJob == null) {
			currentTime = nextTime;
			return;
		}

		activeJob.properties[_TIMELEFT] -= (nextTime - currentTime);
		currentTime = nextTime;

		if (activeJob.properties[_TIMELEFT] == 0) {
			activeJob.finished = nextTime;
			finishedJobs.add(activeJob);
			activeJob = null;
		} else if (roundRobin) {
			updateJobs();
			presentJobs.add(activeJob);
			activeJob = null;
		}
	}

	public ArrayList<Work> schedule() {
		currentTime = futureJobs.get(0).properties[_ARRIVAL];

		while (true) {
			updateJobs();
			chooseJob();
			nextEvent();

			if (nextTime == _NONE)
				break;

			logWork();
			executeJob();
		}

		return finalSchedule;
	}

	// Performance
	public float averageServiceTime() {
		int s = 0;
		for (Job j : finishedJobs)
			s += j.serviceTime();
		
		return (float) s / finishedJobs.size();
	}

	public float averageWaitTime() {
		int s = 0;
		for (Job j : finishedJobs)
			s += j.waitTime();
		
		return (float) s / finishedJobs.size();
	}

	// API
	public void configureAlgorithm(boolean _preemptive, byte[] _criteria) {
		roundRobin = false;
		preemptive = _preemptive;
		criteria = _criteria;
	}

	public void configureAlgorithmRR(int rrQuantum) {
		roundRobin = true;
		preemptive = false;
		quantum = rrQuantum;
		for (byte i = 0; i < 3; i++)
			criteria[i] = NONE;
	}

	public void configureQueue(ArrayList<Job> queue) {
		finishedJobs = new ArrayList<>();
		presentJobs = new ArrayList<>();
		futureJobs = queue;
		finalSchedule = new ArrayList<>();
	}

	// Console Input
	public void inputAlgorithm(Scanner input) {
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
			roundRobin = true;
			do {
				System.out.print("Quantum: ");
				quantum = input.nextInt();
			} while (quantum < 1);
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
		}
	}

	public void inputQueue(Scanner input) {
		finishedJobs = new ArrayList<>();
		presentJobs = new ArrayList<>();
		futureJobs = new ArrayList<>();
		finalSchedule = new ArrayList<>();

		int n;
		do {
			System.out.print("Number of Jobs: ");
			n = input.nextInt();
		} while (n < 1);

		while (futureJobs.size() < n) {
			System.out.println("- Job " + (futureJobs.size() + 1) + ":");
			futureJobs.add(new Job(input));
		}

		System.out.println();
	}

	// Console Output
	public void outputSchedule() {
		for (Work w : finalSchedule) {
			String activeJobId = (w.job == null) ? "Nothing" : w.job.id;
			System.out.printf("%2d - %2d: %s\n", w.from, w.to, activeJobId);
		}
	}

	public void outputFinished() {
		for (Job j : finishedJobs) {
			System.out.println("- Job ID: " + j.id + "\n" +
				"	Arrived     : " + j.properties[_ARRIVAL] + "\n" +
				"	Finished    : " + j.finished + "\n" +
				"	Wait time   : " + j.waitTime() + "\n" +
				"	Service time: " + j.serviceTime()
			);
		}
	}

}
