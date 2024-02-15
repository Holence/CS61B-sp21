package gitlet;

import static gitlet.Utils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Holence
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */

    public static void checkOperands(String[] args, int num) {
        if (args.length != num) {
            message("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void checkOperands(String arg, String exact) {
        if (!arg.equals(exact)) {
            message("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            message("Please enter a command.");
            System.exit(0);
        }

        switch (args[0]) {
            case "init":
                checkOperands(args, 1);
                Repository.init();
                break;
            case "add":
                Repository.checkInitialized();
                checkOperands(args, 2);
                Repository.add(args[1]);
                break;
            case "commit":
                Repository.checkInitialized();
                checkOperands(args, 2);
                Repository.commit(args[1]);
                break;
            case "rm":
                Repository.checkInitialized();
                checkOperands(args, 2);
                Repository.remove(args[1]);
                break;
            case "log":
                Repository.checkInitialized();
                checkOperands(args, 1);
                Repository.log();
                break;
            case "show":
                Repository.checkInitialized();
                checkOperands(args, 2);
                Repository.show(args[1]);
                break;
            case "global-log":
                Repository.checkInitialized();
                checkOperands(args, 1);
                Repository.globalLog();
                break;
            case "find":
                Repository.checkInitialized();
                checkOperands(args, 2);
                Repository.find(args[1]);
                break;
            case "status":
                Repository.checkInitialized();
                checkOperands(args, 1);
                Repository.status();
                break;
            case "checkout":
                Repository.checkInitialized();
                switch (args.length) {
                    // checkout -- [filename]
                    case 3:
                        checkOperands(args[1], "--");
                        Repository.checkoutFileInHeadDCommit(args[2]);
                        break;
                    // checkout [commitHashID] -- [filename]
                    case 4:
                        checkOperands(args[2], "--");
                        Repository.checkoutFileInCommit(args[1], args[3]);
                        break;
                    // checkout [branchname]
                    case 2:
                        Repository.checkoutBranch(args[1]);
                        break;
                    default:
                        message("Incorrect operands.");
                        System.exit(0);
                }
                break;
            case "branch":
                Repository.checkInitialized();
                checkOperands(args, 2);
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                Repository.checkInitialized();
                checkOperands(args, 2);
                Repository.removeBranch(args[1]);
                break;
            case "reset":
                Repository.checkInitialized();
                checkOperands(args, 2);
                Repository.reset(args[1]);
                break;
            case "merge":
                Repository.checkInitialized();
                checkOperands(args, 2);
                Repository.merge(args[1]);
                break;
            default:
                message("No command with that name exists.");
        }
    }
}
