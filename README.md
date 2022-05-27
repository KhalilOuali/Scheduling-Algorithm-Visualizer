# Scheduling-Algorithm-Visualizer
A java/javafx program which visualizes a few process scheduling algorithms, for teaching/learning purposes.

## Features
### Supported scheduling algorithms
* **First come first serve**.
* **Shortest job first** (preemptive and non-preemptive).
* **Priority** (preemptive and non-preemptive).
* **Round Robin**.

You can add secondary selection criteria for the FCFS, SJF and Priority algorithms. 
*For example, if you choose FCFS and two jobs arrive at the same time, you can configure the algorithm to choose whichever is shortest, or whichever has priority.*

### Output
* Gantt diagram of the processor's schedule, specifying which job is being done during each time frame.
* Average turnaround time.
* Average wait time.
* Wait time, turnaround time, and finish time for each process.

## GUI Version
* Clear schedule with color-matched time frames.
* Tooltip indicating process's status at the end of each time frame (Hover the cursor over the time frame).
* Full input checking with error messages (except unique process IDs).

![image](https://user-images.githubusercontent.com/68998620/170784156-a8d159c1-83aa-4a2e-83a2-a9b3e7fad5a4.png)

The GUI version runs on JavaFX. If you're compiling it on your own, or if you're running it without the bundled JRE, make sure to have JavaFX with your local JDK or JRE. You can download [Java 8](https://www.java.com/en/download/) which includes JavaFX.

## Console Version

![image](https://user-images.githubusercontent.com/68998620/170794855-9228acf3-de6d-4416-9134-e63f70b3cae8.png)

The console version lacks format checking, so please make sure to enter valid formats.  
(integer numbers when required, true/false for booleans, no spaces in strings)

## Usage
1. Download the `.zip` of your choice (Windows or Linux, GUI or CLI) from the releases page.
2. Run the application.
3. Input the processes' details: 
	* Number of processes (Console version)
	* Process ID : the name/identifier of the process.
	* Time of arrival (≥ 0) : when the process was launched.
	* CPU time (> 0) : how long the process will take.
	* Priority (smaller value corresponds to higher priority) : used for priority scheduling.
4. Configure the algorithm:
	* Choose the main scheduling method.
	* Choose whether or not it's preemptive (if applicable).
	* Choose any secondary selection criteria (if applicable).
	* Choose the quantum (if the algorithm is Round Robin).
5. View the processor's schedule
6. View information about each of the processes' execution (Console version).
7. View the average wait and turnaround times.

## Source code files
* `Scheduler.java` simulates the process scheduler. It provides methods for configuring the job queue and the schduling algorithm, as well as returning the final schedule.
* `ConsoleApp.java` provides a text-based interface for the scheduler.
* `GUIApp.java` provides a graphical user interface for the scheduler, using JavaFX.

---
### Other information
Icons from [google/material-design-icons](https://github.com/google/material-design-icons).
