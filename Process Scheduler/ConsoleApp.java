import java.util.Scanner;

public class ConsoleApp {
	public static void main(String[] args) {

		try (Scanner input = new Scanner(System.in)) {
			Scheduler procSched = new Scheduler();
			procSched.inputQueue(input);
			procSched.inputAlgorithm(input);

			procSched.schedule();

			System.out.println("\n---------------- CPU Schedule:");
			procSched.outputSchedule();

			boolean showJobs;
			System.out.print("\n(true | false) Show final jobs' state? ");
			showJobs = input.nextBoolean();
			if (showJobs) {
				System.out.println("\n---------------- Final results:");
				procSched.outputFinished();
			}

			System.out.println("\n---------------- Performance:");
			System.out.printf("Average service time: %.2f\n", procSched.averageServiceTime());
			System.out.printf("Average wait time:  %.2f\n", procSched.averageWaitTime());
		}

		System.out.println();
	}
}
