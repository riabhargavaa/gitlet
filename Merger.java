package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static gitlet.Utils.join;

public class Merger extends Repository{
    private static final File REMOTE_FILE = Utils.join(GIT, "remoteFile");;

    public static void wherefail(String b, HashMap<String, byte[]> stagedFiles, HashMap<String, byte[]> removeFiles, HashMap<String, String> allBranches,
                                 String curBranch, Commit master, List<String> allWorkingFiles) {

        if (!stagedFiles.isEmpty() || !removeFiles.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (!allBranches.containsKey(b)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (curBranch.equals(b)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        for (String i: allWorkingFiles) {
            if (!master.getCommitObject().containsKey(i)) {
                String workingHash = Utils.sha1(Utils.readContentsAsString(Utils.join(CWD, i)));
                File searchFile = Utils.join(COMMIT_FOLDER, allBranches.get(b));
                Commit searchCommit = Utils.readObject(searchFile, Commit.class);

                if (searchCommit.getCommitObject().containsKey(i)) {
                    if (!Utils.sha1(searchCommit.getCommitObject().get(i)).equals(workingHash)) {
                        System.out.println("There is an untracked file in the way; "
                                + "delete it, or add and commit it first.");
                        System.exit(0);
                    }
                }
            }
        }
    }

    public static void tester(String b, HashMap<String, String> allBranches, Commit master) {
        File searchFile = Utils.join(COMMIT_FOLDER, allBranches.get(b));
        Commit searchCommit = Utils.readObject(searchFile, Commit.class);
        String masterHash = Utils.sha1(master.getList());

        while (searchCommit != null) {
            if (masterHash.equals(Utils.sha1(searchCommit.getList()))) {
                System.out.println("Current branch fast-forwarded.");
                switchs(b);
                System.exit(0);
            } else if (searchCommit.getParent() == null) {
                searchCommit = null;
            } else {
                File prev = Utils.join(COMMIT_FOLDER, searchCommit.getParent());
                searchCommit = Utils.readObject(prev, Commit.class);
            }
        }

        searchCommit = Utils.readObject(searchFile, Commit.class);
        String searchHash = Utils.sha1(searchCommit.getList());
        Commit c = master;

        while (c != null) {
            if (searchHash.equals(Utils.sha1(c.getList()))) {
                System.out.println("Given branch is an ancestor of the current branch.");
                System.exit(0);
            } else if (c.getParent() == null) {
                c = null;
            } else {
                File prev = Utils.join(COMMIT_FOLDER, c.getParent());
                c = Utils.readObject(prev, Commit.class);
            }
        }

        //return searchCommit;
    }

    public static String whererere(String b, HashMap<String, String> allBranches, Commit master) {

        File prevFile = HOLDEROFMAIN;
        File searchFile = Utils.join(COMMIT_FOLDER, allBranches.get(b));
        Commit searchCommit = Utils.readObject(searchFile, Commit.class);
        Commit curCommit = master;

        String searchHash = Utils.sha1(searchCommit.getList());
        String masterHash = Utils.sha1(master.getList());

        LinkedList<String> brList = new LinkedList<>();
        LinkedList<String> curList = new LinkedList<>();
        HashSet<String> markers = new HashSet<>();

        boolean keepGoing = true;

        brList.add(searchHash);
        curList.add(masterHash);

        while (true) {
            if (keepGoing) {
                String y = curList.removeFirst();

                if (markers.contains(y)) {
                    return y;
                }

                markers.add(y);
                keepGoing = false;

                if (curCommit.getParent() != null) {
                    curList.add(curCommit.getParent());
                }
                if (curCommit.getSecondParent() != null) {
                    curList.add(curCommit.getSecondParent());
                }
                if (curCommit.getParent() == null) {
                    continue;
                }
                prevFile = Utils.join(COMMIT_FOLDER, curCommit.getParent());
                curCommit = Utils.readObject(prevFile, Commit.class);
                masterHash = Utils.sha1(curCommit.getList());
                curList.add(masterHash);
            } else {
                String n = brList.removeFirst();
                if (markers.contains(n)) {
                    return n;
                }
                markers.add(n);
                keepGoing = true;
                if (searchCommit.getParent() != null) {
                    brList.add(searchCommit.getParent());
                }
                if (searchCommit.getSecondParent() == null) {
                    brList.add(searchCommit.getSecondParent());
                }
                if (searchCommit.getParent() == null) {
                    continue;
                }

                searchFile = Utils.join(COMMIT_FOLDER, searchCommit.getParent());
                searchCommit = Utils.readObject(prevFile, Commit.class);
                searchHash = Utils.sha1(searchCommit.getList());
                brList.add(searchHash);
            }
        }
    }

    public static boolean doublecheker(Commit splitCommit, boolean conflict,
                                       Commit searchCommit, Commit mergeCommit, Commit master) {
        for (String i : splitCommit.getCommitObject().keySet()) {
            conflict = mastermerger(splitCommit,
                    conflict, searchCommit, mergeCommit, i, master);

            if (seoncf(splitCommit,
                    conflict, searchCommit, mergeCommit, i, master)) {
                continue;
            }
            if (fifthCase(splitCommit, searchCommit, i, master)) {
                continue;
            }
            conflict = searchMergeCheck(splitCommit,
                    conflict, searchCommit, mergeCommit, i, master);
            if (secondSearchMergeCheck(splitCommit,
                    conflict, searchCommit, mergeCommit, i, master)) {
                continue;
            }
            if (firstCase(splitCommit, searchCommit, mergeCommit, i, master)) {
                continue;
            }
            if (secondCase(splitCommit, searchCommit, mergeCommit, i, master)) {
                continue;
            }
            if (!master.getCommitObject().containsKey(i)
                    && !searchCommit.getCommitObject().containsKey(i)) {
                continue;
            }
            conflict = sixthCase(splitCommit,
                    conflict, searchCommit, mergeCommit, i, master);
        }
        return conflict;
    }

    public static boolean mastermerger(Commit splitCommit, boolean conflict,
                                       Commit searchCommit, Commit mergeCommit, String i, Commit master) {
        if (master.getCommitObject().get(i) == null) {
            if (!Utils.sha1(splitCommit.getCommitObject().get(i)).equals
                    (Utils.sha1(searchCommit.getCommitObject().get(i)))) {
                byte[] head = "<<<<<<< HEAD\n".getBytes();
                byte[] curContent = {};
                byte[] lineBreak = "=======\n".getBytes();
                byte[] newContent = searchCommit.getCommitObject().get(i);
                byte[] close = ">>>>>>>\n".getBytes();

                byte[] p1 = addArrays(head, curContent);
                byte[] p2 = addArrays(p1, lineBreak);
                byte[] p3 = addArrays(p2, newContent);
                byte[] p4 = addArrays(p3, close);

                mergeCommit.addCommit(i, p4);
                conflict = true;
                File newFile = Utils.join(CWD, i);
                Utils.writeContents(newFile, p4);
            } else {
                mergeCommit.addCommit(i, searchCommit.getCommitObject().get(i));
            }
        }
        return conflict;
    }

    public static boolean seoncf(
            Commit splitCommit, boolean conflict,
            Commit searchCommit, Commit mergeCommit, String i, Commit master) {
        boolean check = false;
        if (master.getCommitObject().get(i) == null) {
            check = true;
            if (!Utils.sha1(splitCommit.getCommitObject().get(i)).equals(master.getCommitObject().get(i))) {
                byte[] head = "<<<<<<< HEAD\n".getBytes();
                byte[] curContent = master.getCommitObject().get(i);
                byte[] lineBreak = "=======\n".getBytes();
                byte[] newContent = {};
                byte[] close = ">>>>>>>\n".getBytes();

                byte[] p1 = addArrays(head, curContent);
                byte[] p2 = addArrays(p1, lineBreak);
                byte[] p3 = addArrays(p2, newContent);
                byte[] p4 = addArrays(p3, close);

                mergeCommit.addCommit(i, p4);
                conflict = true;
                File newFile = Utils.join(CWD, i);
                Utils.writeContents(newFile, p4);
            } else {
                mergeCommit.addCommit(i, master.getCommitObject().get(i));
            }
        }
        return check;
    }

    public static boolean firstCase(Commit splitCommit, Commit searchCommit,
                                    Commit mergeCommit, String i, Commit master) {
        boolean conflict = false;
        String splitHash = Utils.sha1(splitCommit.getCommitObject().get(i));
        String masterHash = Utils.sha1(master.getCommitObject().get(i));
        String searchHash = Utils.sha1(searchCommit.getCommitObject().get(i));

        if (splitHash.equals(masterHash)) {
            if (!splitHash.equals(searchHash)) {
                conflict = true;
                String searchCommitHash = Utils.sha1(searchCommit.getList());
                prevRestore(searchCommitHash, i);
                mergeCommit.addCommit(i, searchCommit.getCommitObject().get(i));
            }
        }
        return conflict;
    }

    public static boolean secondCase(Commit splitCommit, Commit searchCommit,
                                     Commit mergeCommit, String i, Commit master) {
        boolean conflict = false;
        String splitHash = Utils.sha1(splitCommit.getCommitObject().get(i));
        String masterHash = Utils.sha1(master.getCommitObject().get(i));
        String searchHash = Utils.sha1(searchCommit.getCommitObject().get(i));

        if (!splitHash.equals(masterHash)) {
            if (splitHash.equals(searchHash) || masterHash.equals(searchHash)) {
                conflict = true;
                mergeCommit.addCommit(i, master.getCommitObject().get(i));
                File newVersion = Utils.join(CWD, i);
                Utils.writeContents(newVersion, master.getCommitObject().get(i));
            }
        }
        return conflict;
    }

    public static boolean thirdCase(Commit splitCommit, boolean conflict,
                                    Commit searchCommit, Commit mergeCommit, String i, Commit master) {
        if (!splitCommit.getCommitObject().containsKey(i)
                && !searchCommit.getCommitObject().containsKey(i)) {
            mergeCommit.addCommit(i, master.getCommitObject().get(i));
            File newVersion = Utils.join(CWD, i);
            Utils.writeContents(newVersion, master.getCommitObject().get(i));
        } else if (!splitCommit.getCommitObject().containsKey(i)
                && searchCommit.getCommitObject().containsKey(i)) {
            byte[] head = "<<<<<<< HEAD\n".getBytes();
            byte[] curContent = master.getCommitObject().get(i);
            byte[] lineBreak = "=======\n".getBytes();
            byte[] newContent = searchCommit.getCommitObject().get(i);
            byte[] close = ">>>>>>>\n".getBytes();

            byte[] p1 = addArrays(head, curContent);
            byte[] p2 = addArrays(p1, lineBreak);
            byte[] p3 = addArrays(p2, newContent);
            byte[] p4 = addArrays(p3, close);

            mergeCommit.addCommit(i, p4);
            conflict = true;
            File newVersion = Utils.join(CWD, i);
            Utils.writeContents(newVersion, p4);
        }
        return conflict;
    }

    public static void fourthCase(Commit splitCommit, Commit searchCommit,
                                  Commit mergeCommit, String i, Commit master) {
        if (!splitCommit.getCommitObject().containsKey(i)
                && !master.getCommitObject().containsKey(i)) {
            String searchHash = Utils.sha1(searchCommit.getList());
            prevRestore(searchHash, i);
            add(i);
            mergeCommit.addCommit(i, searchCommit.getCommitObject().get(i));

            File newVersion = Utils.join(CWD, i);
            Utils.writeContents(newVersion, searchCommit.getCommitObject().get(i));
        }
    }

    public static boolean fifthCase(Commit splitCommit,
                                    Commit searchCommit, String i, Commit master) {
        boolean conflict = false;
        String splitHash = Utils.sha1(splitCommit.getCommitObject().get(i));
        if (master.getCommitObject().get(i) == null) {
            return false;
        }
        String masterHash = Utils.sha1(master.getCommitObject().get(i));

        if (splitHash.equals(masterHash)) {
            if (searchCommit.getCommitObject().get(i) == null) {
                rm(i);
                conflict = true;
            }
        }
        return conflict;
    }

    public static boolean sixthCase(Commit splitCommit, boolean conflict,
                                    Commit searchCommit, Commit mergeCommit, String i,Commit master) {
        String splitHash = Utils.sha1(splitCommit.getCommitObject().get(i));
        String masterHash = Utils.sha1(master.getCommitObject().get(i));
        String searchHash = Utils.sha1(searchCommit.getCommitObject().get(i));

        if (!splitHash.equals(masterHash)) {
            if (!masterHash.equals(searchHash)) {
                byte[] head = "<<<<<<< HEAD\n".getBytes();
                byte[] curContent = master.getCommitObject().get(i);
                byte[] lineBreak = "=======\n".getBytes();
                byte[] newContent = searchCommit.getCommitObject().get(i);
                byte[] close = ">>>>>>>\n".getBytes();

                byte[] p1 = addArrays(head, curContent);
                byte[] p2 = addArrays(p1, lineBreak);
                byte[] p3 = addArrays(p2, newContent);
                byte[] p4 = addArrays(p3, close);

                mergeCommit.addCommit(i, p4);
                conflict = true;
                File newVersion = Utils.join(CWD, i);
                Utils.writeContents(newVersion, p4);
            }
        }
        return conflict;
    }

    public static boolean searchMergeCheck(
            Commit splitCommit, boolean conflict,
            Commit searchCommit, Commit mergeCommit, String i, Commit master) {
        String splitHash = Utils.sha1(splitCommit.getCommitObject().get(i));
        if (master.getCommitObject().get(i) == null) {
            return false;
        }
        String masterHash = Utils.sha1(master.getCommitObject().get(i));

        if (searchCommit.getCommitObject().get(i) == null) {
            if (!splitHash.equals(masterHash)) {
                byte[] head = "<<<<<<< HEAD\n".getBytes();
                byte[] curContent = master.getCommitObject().get(i);
                byte[] lineBreak = "=======\n".getBytes();
                byte[] newContent = {};
                byte[] close = ">>>>>>>\n".getBytes();

                byte[] p1 = addArrays(head, curContent);
                byte[] p2 = addArrays(p1, lineBreak);
                byte[] p3 = addArrays(p2, newContent);
                byte[] p4 = addArrays(p3, close);

                mergeCommit.addCommit(i, p4);
                conflict = true;
                File newVersion = Utils.join(CWD, i);
                Utils.writeContents(newVersion, p4);
            } else {
                mergeCommit.addCommit(i, master.getCommitObject().get(i));
            }
        }
        return conflict;
    }

    public static boolean secondSearchMergeCheck(
            Commit splitCommit, boolean conflict,
            Commit searchCommit, Commit mergeCommit, String i, Commit master) {
        boolean mergeCheck = false;
        String splitHash = Utils.sha1(splitCommit.getCommitObject().get(i));
        if (master.getCommitObject().get(i) == null) {
            return false;
        }
        String masterHash = Utils.sha1(master.getCommitObject().get(i));

        if (searchCommit.getCommitObject().get(i) == null) {
            mergeCheck = true;

            if (!splitHash.equals(masterHash)) {
                byte[] head = "<<<<<<< HEAD\n".getBytes();
                byte[] curContent = master.getCommitObject().get(i);
                byte[] lineBreak = "=======\n".getBytes();
                byte[] newContent = {};
                byte[] close = ">>>>>>>\n".getBytes();

                byte[] p1 = addArrays(head, curContent);
                byte[] p2 = addArrays(p1, lineBreak);
                byte[] p3 = addArrays(p2, newContent);
                byte[] p4 = addArrays(p3, close);

                mergeCommit.addCommit(i, p4);
                conflict = true;
                File newVersion = Utils.join(CWD, i);
                Utils.writeContents(newVersion, p4);
            } else {
                mergeCommit.addCommit(i, master.getCommitObject().get(i));
            }
        }
        return mergeCheck;
    }

    public static byte[] addArrays(byte[] a, byte[] b) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }

        byte[] sum = new byte[a.length + b.length];
        System.arraycopy(a, 0, sum, 0, a.length);
        System.arraycopy(b, 0, sum, a.length, b.length);
        return sum;
    }
    public static void reset(String commitid) {
        Commit master = Utils.readObject(HOLDEROFMAIN, Commit.class);
        String curBranch = Utils.readContentsAsString(currentbranchmain);
        HashMap<String, String> allBranches = Utils.readObject(ALLBRANCHES, HashMap.class);
        LinkedList<String> allCommits = Utils.readObject(CURR_COMMIT, LinkedList.class);
        List<String> allWorkingFiles = Utils.plainFilenamesIn(CWD);

        //Restores all the files tracked by the given commit. Removes tracked files that are not present in that commit
        if(!allCommits.contains(commitid)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        for (String i : allWorkingFiles) {
            if (!master.getCommitObject().containsKey(i)) {
                String workingHash = Utils.sha1(Utils.readContentsAsString(join(CWD, i)));
                File searchFile = join(COMMIT_FOLDER, commitid);
                Commit searchCommit = Utils.readObject(searchFile, Commit.class);
                //System.out.println(searchCommit.getBlobs().get(i));

                if (searchCommit.getCommitObject().containsKey(i)) {
                    if (!Utils.sha1(searchCommit.getCommitObject().get(i)).equals(workingHash)) {
                        System.out.println("There is an untracked file in the way; "
                                + "delete it, or add and commit it first.");
                        System.exit(0);
                    }
                }
            }
        }
        File prev = join(COMMIT_FOLDER, commitid);
        Commit curCommit = Utils.readObject(prev, Commit.class);
        for (String i : curCommit.getCommitObject().keySet()) {
            prevRestore(commitid, i);
        }

        for (String i : master.getCommitObject().keySet()) {
            if (!curCommit.getCommitObject().containsKey(i)) {
                rm(i);
            }
        }

        master = curCommit;
        allBranches.replace(curBranch, commitid);

        HashMap<String, byte[]> stagedFiles = Utils.readObject(stager, HashMap.class);
        HashMap<String, byte[]> removeFiles = Utils.readObject(REMOVEFILE, HashMap.class);
        Utils.writeObject(HOLDEROFMAIN, master);
        Utils.writeContents(currentbranchmain, curBranch);
        Utils.writeContents(REPOSITORY, commitid);
        Utils.writeObject(ALLBRANCHES, allBranches);

        if (!(stagedFiles == null)) {
            stagedFiles.clear();
        }
        if (!(removeFiles == null)) {
            removeFiles.clear();
        }

        Utils.writeObject(stager, stagedFiles);
        Utils.writeObject(REMOVEFILE, removeFiles);
    }
}
