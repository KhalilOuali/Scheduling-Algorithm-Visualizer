import java.util.ArrayList;
import java.util.Collections;

public class Scheduler {
	// Global Constants
	protected static final byte _NONE = -1;
	protected static final byte _ARRIVAL = 0;
	protected static final byte _TIMELEFT = 1;
	protected static final byte _PRIORITY = 2;

	public static final byte NONE = -1;
	public static final byte FCFS = 0;
	public static final byte SJF = 1;
	public static final byte PRIORITY = 2;
	public static final byte ROUNDROBIN = 3;

	// JOB CLASS: processes
	public class Job implements Comparable<Job> {
		String id;
		protected int[] properties = new int[3];
		private int cpuTime;
		public int finished;
		public int order;

		// Constructor
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
		public int turnAroundTime() {
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
	protected boolean roundRobin = false;
	protected boolean preemptive = false;
	protected byte[] criteria = { FCFS, NONE, NONE };
	protected int quantum = 2;

	// Scheduling Variables
	private int currentTime, nextTime;
	private Job activeJob = null;

	// Queue Variables
	protected ArrayList<Job> finishedJobs;
	protected ArrayList<Job> presentJobs;
	protected ArrayList<Job> futureJobs;
	protected ArrayList<Work> finalSchedule;

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
	public float averageTurnAroundTime() {
		int s = 0;
		for (Job j : finishedJobs)
			s += j.turnAroundTime();

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

}
