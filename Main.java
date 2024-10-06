package gitlet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if(args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        // TODO: what if args is empty?
        String firstArg = args[0];
        String second = null;
        String third = null;
        if(args.length == 2) {
            second = args[1];
        } if (args.length == 3) {
            third = args[2];
        }

        switch(firstArg) {
            case "init":
                Repository.init();
                break;
            case "merge":
                Repository.merge(second);
                break;
            case "add":
                Repository.add(second);
                // TODO: handle the `add [filename]` command
                break;
            // TODO: FILL THE REST IN
            case "rm":
                Repository.rm(second);
                break;
            case "reset":
                Merger.reset(second);
                break;
            case "commit":
                Repository.commit(second);
                break;
            case "log":
                Repository.log();
                break;
            case "global-log":
                Repository.globalLog();
                break;
            case "find":
                Repository.find(second);
                break;
            case "status":
                Repository.status();
                break;
            case "rm-branch":
                Repository.removebranchs(second);
                break;
            case "switch":
                Repository.switchs(second);
                break;
            case "branch":
                Repository.branch(second);
                break;
            case "restore":
                if (args.length != 2 && args.length != 4 && args.length != 3) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                } else if (args.length == 2) {
                    Repository.restore(args[1]);
                } else if (args.length == 3) {
                    if (!args[1].equals("--")) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    Repository.restore(args[2]);
                } else {
                    if (!args[2].equals("--")) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    Repository.prevRestore(args[1], args[3]);
                }
                break;

        }
        List<String> commands = new ArrayList<>();
        commands.add("init");
        commands.add("add");
        commands.add("commit");
        commands.add("log");
        commands.add("rm");
        commands.add("global-log");
        commands.add("find");
        commands.add("status");
        commands.add("restore");
        commands.add("reset");
        commands.add("branch");
        commands.add("switch");
        commands.add("rm-branch");
        commands.add("merge");
        for(String comma : commands) {
            if(comma.equals(firstArg)) {
                return;
            }
        }
        System.out.println("No command with that name exists.");
        System.exit(0);
    }
}