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

    public static void main(String[] args) {
        if (args.length == 0) {
            message("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];

        switch (firstArg) {
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
        case "branch":
            Repository.checkInitialized();
            checkOperands(args, 2);
            Repository.branch(args[1]);
            break;
        default:
            message("No command with that name exists.");
        }
    }
}
