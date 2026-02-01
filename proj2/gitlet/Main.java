package gitlet;
import static gitlet.Repository.*;
import static gitlet.Stage.*;
import static gitlet.Commit.*;
import static gitlet.Branch.*;
/** Driver class for Gitlet, a subset of the Git version-control system.
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
          if (args.length != 1) {
              throw new GitletException("Incorrect operands.");
              return;
          }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                if (args.length != 1) {
                    throw new GitletException("Incorrect operands.");
                }
                init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                String addFileName = args[1];
                add(addFileName);
                break;
            case "commit":
                String commitMessage = args[1];
                commit(commitMessage);
            case "rm":
                String rmFileName = args[1];
                remove(rmFileName);
                break;
           case "log":
               if(args.length != 1) {
                   throw new GitletException("Incorrect operands.");
               }
               log();
                break;
           case "global-log":
                if(args.length != 1) {
                     throw new GitletException("Incorrect operands.");
                    }
                globalLog();
                break;
           case "find":
                String findMessage = args[1];
                find(findMessage);
               break;
           case "status":
                if(args.length != 1) {
                     throw new GitletException("Incorrect operands.");
                }
                status();
               break;
           case "checkout":
               if(args.length == 1){
                     throw new GitletException("Incorrect operands.");
               }
               checkOut(args);
               break;
           case "branch":
               if(args.length != 2) {
                   throw new GitletException("Incorrect operands.");
               }
               createBranch(args[1]);
               break;
           case "rm-branch":
                if(args.length != 2) {
                     throw new GitletException("Incorrect operands.");
                }
                removeBranch(args[1]);
               break;
           case "reset":
                if(args.length != 2) {
                     throw new GitletException("Incorrect operands.");
                }
                reset(args[1]);
               break;
           case "merge":
                if(args.length != 2) {
                     throw new GitletException("Incorrect operands.");
                }
                merge(args[1]);
               break;
           default:
                throw new GitletException("No command with that name exists.");
        }
    }
}
