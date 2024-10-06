package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository implements Serializable{
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GIT = Utils.join(CWD, ".gitlet");
    public static final File REPOSITORY = Utils.join(GIT, "repository");

    public static final File COMMIT_FOLDER = Utils.join(GIT, "CommitFile");
    public static final File CURR_COMMIT = Utils.join(GIT, "Main");
    public static final File currentbranchmain = Utils.join(GIT, "branch");
    public static final File STAGINGAREA = Utils.join(GIT, "StagingArea");
    public static final File CURRBLOBS = Utils.join(GIT, "Blobs");
    public static final File HOLDEROFMAIN = Utils.join(GIT, "Holder");
    public static final File remover = Utils.join(GIT, "toRemove");
    public static final File ALLBRANCHES = Utils.join(GIT, "ALLBRANCH");
    public static final File REMOVEFILE = Utils.join(GIT, "REMOVEFILE");
    public static final File SPLITEE = Utils.join(GIT, "SPLITS");
    public static final File stager = Utils.join(GIT, "StageAdd");
    public static void init() {
        if (GIT.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
        }
        //make the directories
        //HEAD.mkdirs();
        GIT.mkdirs();
        STAGINGAREA.mkdirs();
        COMMIT_FOLDER.mkdirs();
        CURRBLOBS.mkdirs();
        //make repository
        HashMap<String, String> asallBranches = new HashMap<>();
        LinkedList<String> allCommits = new LinkedList<>();
        HashMap<String, byte[]> stagedFiles = new HashMap<>();
        HashMap<String, byte[]> removeFiles = new HashMap<>();
        HashMap<String, byte[]> removeMerge = new HashMap<>();

        Commit c =  new Commit("initial commit", null, new HashMap<>());
        String id = Utils.sha1(c.getList());
        makeFile(c, id);
        allCommits.add(id);
        Utils.writeObject(CURR_COMMIT, allCommits);
        //set main branch as c
        asallBranches.put("main", id);
        Utils.writeContents(currentbranchmain, "main");
        Utils.writeContents(REPOSITORY, id);
        Utils.writeObject(stager, stagedFiles);
        Utils.writeObject(CURR_COMMIT, allCommits);
        Utils.writeObject(ALLBRANCHES, asallBranches);
        Utils.writeObject(HOLDEROFMAIN, c);
        Utils.writeObject(REMOVEFILE, removeFiles);
        Utils.writeObject(SPLITEE, c);
        Utils.writeObject(remover, removeMerge);
//        Utils.writeContents(CURRBRANCH, "main");
//        Utils.writeContents(REPOSITORY, (id));
//        Utils.writeObject(CURR_COMMIT, allCommits);
//        Utils.writeObject(STAGINGAREA, stagedFiles);
//        Utils.writeObject(CURRBRANCH, asallBranches);
//        Utils.writeObject(REMOVE_FILE, removeFiles);
//        Utils.writeObject(REMOVE_MERGE, removeMerge);
        }

    public static void makeFile(Commit toAdd, String id) {
        File toadd = join(COMMIT_FOLDER, id);
        Utils.writeObject(toadd, toAdd);
    }

    public static void add(String f) {
        File toadd = new File(CWD, f);
        if(!toadd.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        Commit currhead = Utils.readObject(HOLDEROFMAIN, Commit.class);
        HashMap<String, byte[]> r = Utils.readObject(REMOVEFILE, HashMap.class);
        //checks if the remove file contains the current file
        if(r.containsKey(f)) {
            r.remove(f);
            //update the removefile wrapper
            Utils.writeObject(REMOVEFILE, r);
        }
        if(currhead.getCommitObject().containsKey(f)) {
            //gets the id of the f object in current blobs
            String mainid = Utils.sha1(currhead.getCommitObject().get(f));
            //gets the current f id
            String currid = Utils.sha1(Utils.readContents(toadd));
            if(mainid.equals(currid)) {
                //set main to curr file
                Utils.writeObject(HOLDEROFMAIN, currhead);
                //get the staged files map of commits
                HashMap<String, byte[]> mapstage = Utils.readObject(stager, HashMap.class);
                mapstage.remove(f);
                Utils.writeObject(stager, mapstage);
                System.exit(0);

                r.remove(f);
                Utils.writeObject(REMOVEFILE, r);
                System.exit(0);
            }
        }
        //update the holder file
        Utils.writeObject(HOLDEROFMAIN, currhead);
        //gets staged file
        HashMap<String, byte[]> mapstage = Utils.readObject(stager, HashMap.class);
        //adds the file to staging area
        mapstage.put(f, Utils.readContents(toadd));
        //update staging area
        Utils.writeObject(stager, mapstage);
    }
    public static void commit(String m) {
        //Staged
        HashMap<String, byte[]> mapstage = Utils.readObject(stager, HashMap.class);
        //Remove
        HashMap<String, byte[]> removed = Utils.readObject(REMOVEFILE, HashMap.class);
        if (m.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        if(mapstage.isEmpty() && removed.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        String currentHead = Utils.readContentsAsString(REPOSITORY);
        //Commit main = getCommit(currentHead);
        //get current commit
        Commit main = Utils.readObject(HOLDEROFMAIN, Commit.class);
        //get the commits
        HashMap<String, byte[]> mainBlob = main.getCommitObject();
        HashMap<String, byte[]> newCommits = new HashMap<>();
        //fill all new commits with past ones
        if(mainBlob != null) {
            for(String s : mainBlob.keySet()) {
                newCommits.put(s, mainBlob.get(s));
            }
        }
        //add staged files
        for (String s : mapstage.keySet()) {
            newCommits.put(s, mapstage.get(s));
            mapstage =  Utils.readObject(stager, HashMap.class);
            File newtoenter = join(CURRBLOBS, Utils.sha1(mapstage.get(s)));
            Utils.writeContents(newtoenter, mapstage.get(s));
        }
        //empty out staged items
        mapstage = new HashMap<String, byte[]>();
        Utils.writeObject(stager, mapstage);

        //REMOVED PARTS
        HashMap<String, byte[]> removedMerge = Utils.readObject(remover, HashMap.class);
        for(String i : removed.keySet()) {
            newCommits.remove(i);
            removedMerge.put(i, removed.get(i));
        }
        removed = new HashMap<>();
        Utils.writeObject(REMOVEFILE, removed);
        Utils.writeObject(remover, removedMerge);

        //shifting the main to the new commit
        Commit toadd = new Commit(m, Utils.sha1(main.getList()), newCommits);
        main = toadd;

        String newMainid = Utils.sha1(main.getList());
        LinkedList<String> all = null;
        all = Utils.readObject(CURR_COMMIT, LinkedList.class);
        all.add(newMainid);
        Utils.writeObject(CURR_COMMIT, all);
        File add2 = join(COMMIT_FOLDER, newMainid);
        Utils.writeObject(add2, toadd);
        Utils.writeObject(HOLDEROFMAIN, main);
        Utils.writeObject(SPLITEE, toadd);

        //update branches
        String currbranch = Utils.readContentsAsString(currentbranchmain);

        HashMap<String, String> branching = Utils.readObject(ALLBRANCHES, HashMap.class);
        branching.put(currbranch, newMainid);
        Utils.writeObject(ALLBRANCHES, branching);
        Utils.writeContents(currentbranchmain, currbranch);

        currentHead = newMainid;
        Utils.writeContents(REPOSITORY, currentHead);

    }

    public static Commit getCommit(String n) {
        File read = join(COMMIT_FOLDER, n);
        return Utils.readObject(read, Commit.class);
    }

    //RESTORE
    public static void restore(String file) {
        String currId = Utils.readContentsAsString(REPOSITORY);
        File currcom = join(COMMIT_FOLDER, currId);
        Commit curr = Utils.readObject(currcom, Commit.class);
        //checks if file is even in the current commits
        if(!curr.getCommitObject().containsKey(file)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        File tobe = join(CWD,file);
        File toget = join(CURRBLOBS, Utils.sha1(curr.getCommitObject().get(file)));
        Utils.writeContents(tobe, Utils.readContents(toget));
    }
    public static void prevRestore(String commitid, String f) {
        LinkedList<String> allCommits = Utils.readObject(CURR_COMMIT, LinkedList.class);
        //acouning for the ones with length less than 40
        if(commitid.length() < 40) {
            for(String s : allCommits) {
                if(s.startsWith(commitid)) {
                    commitid = s;
                }
            }
        }
       // the prev doesnt exist
        if(!allCommits.contains(commitid)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        File whichone = join(COMMIT_FOLDER, commitid);
        Commit commi = Utils.readObject(whichone, Commit.class);

        if(!commi.getCommitObject().containsKey(f)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        File tobe = join(CWD,f);
        File target = join(CURRBLOBS, Utils.sha1(commi.getCommitObject().get(f)));
        Utils.writeContents(tobe, Utils.readContents(target));
    }


    public static void rm(String f) {
        Commit currhead = Utils.readObject(HOLDEROFMAIN, Commit.class);
        HashMap<String, byte[]> r = Utils.readObject(REMOVEFILE, HashMap.class);
        HashMap<String, byte[]> mapstage = Utils.readObject(stager, HashMap.class);
        File toremove = join(CWD, f);

        //checks if the staging area doesnt contain file and the commits doesnt contain file
        if(!mapstage.containsKey(f) && !currhead.getCommitObject().containsKey(f)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        //checks if the remove file contains the staging file
        mapstage.remove(f);
        //update the staging wrapper
        Utils.writeObject(stager, mapstage);
        //If the file is tracked in the current commit
        //LinkedList<String> allCommits = Utils.readObject(CURR_COMMIT, LinkedList.class);
        if(currhead.getCommitObject().containsKey(f)) {
            //if commits in staging area contains the file then we put in remove list
            r.put(f, currhead.getCommitObject().get(f));
            Utils.restrictedDelete(toremove);
        }
        //System.out.print("removed " + f);
        Utils.writeObject(REMOVEFILE, r);
    }
    public static void log() {
        Commit main = Utils.readObject(HOLDEROFMAIN, Commit.class);
        while(main != null) {
            System.out.println("===");
            String id = Utils.sha1(main.getList());
            String date = main.getDate();
            String mess = main.getMessage();
            if (main.getSecondParent() != null) {
                System.out.println("commit " + id + "\nMerge: " + main.getParent() + " " + main.getSecondParent() + "\nDate: " + date + "\n" + mess);
            } else {
                System.out.println("commit " + id + "\nDate: " + date + "\n" + mess);
            }
            System.out.println();
            if(main.getParent() == null) {
                main = null;
            } else {
                File parent = join(COMMIT_FOLDER, main.getParent());
                main = Utils.readObject(parent, Commit.class);
            }
        }
    }
    public static void globalLog() {
        List<String> toit = Utils.plainFilenamesIn(COMMIT_FOLDER);
        for(String s : toit) {
            File c = join(COMMIT_FOLDER, s);
            Commit cont = readObject(c, Commit.class);
            System.out.println("===");
            System.out.println("commit " + Utils.sha1(cont.getList()) + "\nDate: " + cont.getDate() + "\n" + cont.getMessage());
            System.out.println();
        }
    }
    public static void find(String commitmessage) {
        List<String> toit = Utils.plainFilenamesIn(COMMIT_FOLDER);
        boolean state = false;
        for(String s : toit) {
            //get commit file
            File c = join(COMMIT_FOLDER, s);
            Commit cont = Utils.readObject(c, Commit.class);
            if(cont.getMessage().equals(commitmessage)) {
                state = true;
                System.out.println(s);
            }
        }
        if(!state) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }
    public static void branch(String newbranch) {
        HashMap<String, String> branchlist = readObject(ALLBRANCHES, HashMap.class);
        //find head
        Commit c = readObject(HOLDEROFMAIN, Commit.class);
        if(branchlist.containsKey(newbranch)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        String curr = Utils.sha1(c.getList());
        String rep = Utils.readContentsAsString(REPOSITORY);
        branchlist.put(newbranch, curr);
        Utils.writeObject(SPLITEE, rep);
        Utils.writeObject(ALLBRANCHES, branchlist);
    }
    public static void status() {
        if (!GIT.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        HashMap<String, String> br = Utils.readObject(ALLBRANCHES, HashMap.class);
        HashMap<String, byte[]> staged = Utils.readObject(stager, HashMap.class);
        HashMap<String, byte[]> toremove = Utils.readObject(REMOVEFILE, HashMap.class);
        String curr = Utils.readContentsAsString(currentbranchmain);
        System.out.println("=== Branches ===");
        ArrayList<String> lst = new ArrayList<>();
        lst.sort(String::compareToIgnoreCase);
        Collections.sort(lst);
        for (String i : br.keySet()) {
            lst.add(i);
        }
        Collections.sort(lst);
        for(String s : lst) {
            if(s != null && s.equals(curr)) {
                System.out.println("*" + s);
            } else {
                System.out.println(s);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        ArrayList<String> list2 = new ArrayList<>();
        list2.sort(String::compareToIgnoreCase);
        Collections.sort(list2);
        for (String i : staged.keySet()) {
            list2.add(i);
        }
        Collections.sort(list2);
        for(String s : list2) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        ArrayList<String> list3 = new ArrayList<>();
        Collections.sort(list3);
        list3.sort(String::compareToIgnoreCase);
        for (String i : toremove.keySet()) {
            list3.add(i);
        }
        Collections.sort(list3);
        for(String s : list3) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
    }
    public static void switchs(String name) {
        int count = 1;
        if(count == 1) {
            String currentBranch = Utils.readContentsAsString(currentbranchmain);
            HashMap<String, String> allbranch = Utils.readObject(ALLBRANCHES, HashMap.class);
            String currhead = Utils.readContentsAsString(REPOSITORY);
            Commit main = Utils.readObject(HOLDEROFMAIN, Commit.class);
            HashMap<String, byte[]> stagedFiles = Utils.readObject(stager, HashMap.class);
            HashMap<String, byte[]> torem = Utils.readObject(REMOVEFILE, HashMap.class);
            List<String> allWorkingFiles = Utils.plainFilenamesIn(CWD);
            if (!allbranch.containsKey(name)) {
                System.out.println("No such branch exists.");
                System.exit(0);
            } else if (currentBranch.equals(name)) {
                System.out.println("No need to switch to the current branch.");
                System.exit(0);
            } else {
                for (String i : allWorkingFiles) {
                    if (!main.getCommitObject().containsKey(i)) {
                        String workingHash = Utils.sha1(Utils.readContentsAsString(join(CWD, i)));
                        File searchFile = join(COMMIT_FOLDER, allbranch.get(name));
                        Commit searchCommit = Utils.readObject(searchFile, Commit.class);

                        if (searchCommit.getCommitObject().containsKey(i)) {
                            String is = Utils.sha1(searchCommit.getCommitObject().get(i));
                            if (!is.equals(workingHash)) {
                                System.out.println("There is an untracked file in the way; " + "delete it, or add and commit it first.");
                                System.exit(0);
                            }
                        }
                    }
                }
            }

            String newHead = allbranch.get(name);
            File newHeadPath = join(COMMIT_FOLDER, newHead);
            Commit newCommit = Utils.readObject(newHeadPath, Commit.class);
            for (String i : newCommit.getCommitObject().keySet()) {
                prevRestore(newHead, i);
            }
            for (String i : main.getCommitObject().keySet()) {
                if (!newCommit.getCommitObject().containsKey(i)) {
                    Utils.restrictedDelete(i);
                }
            }
            stagedFiles.clear();
            torem.clear();
            currentBranch = name;
            currhead = allbranch.get(currentBranch);

            File prev = join(COMMIT_FOLDER, currhead);
            main = Utils.readObject(prev, Commit.class);

            writeBack(currentBranch, allbranch, currhead, main, stagedFiles, torem);
        }
    }
    public static void writeBack(String currentBranch, HashMap<String, String> allbranch, String currhead, Commit main, HashMap<String, byte[]> stagedFiles, HashMap<String, byte[]> torem) {
        int count = 0;
        Utils.writeObject(HOLDEROFMAIN, main);
        Utils.writeObject(stager, stagedFiles);
        Utils.writeObject(REMOVEFILE, torem);
        Utils.writeContents(currentbranchmain, currentBranch);
        Utils.writeContents(REPOSITORY, currhead);
        count++;
    }
    public static void removebranchs(String name) {
        HashMap<String, String> allbranch = Utils.readObject(ALLBRANCHES, HashMap.class);
        String currbranch = Utils.readContentsAsString(currentbranchmain);
        if(!allbranch.containsKey(name)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } if(currbranch.equals(name)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        allbranch.remove(name);
        Utils.writeObject(ALLBRANCHES, allbranch);
    }
    public static void merge(String branchName) {
        HashMap<String, String> brnaches = Utils.readObject(ALLBRANCHES, HashMap.class);
        Commit main = Utils.readObject(HOLDEROFMAIN, Commit.class);
        String curHead = Utils.readContentsAsString(REPOSITORY);
        String curBranch = Utils.readContentsAsString(currentbranchmain);
        LinkedList allofthe = Utils.readObject(CURR_COMMIT, LinkedList.class);
        HashMap<String, byte[]> stagedFiles = Utils.readObject(stager, HashMap.class);
        List<String> allWorkingFiles = Utils.plainFilenamesIn(CWD);
        HashMap<String, byte[]> plzwork = Utils.readObject(REMOVEFILE, HashMap.class);
        Merger.wherefail(branchName, stagedFiles, plzwork, brnaches, curBranch, main, allWorkingFiles);
        Merger.tester(branchName, brnaches, main);
        String splitHash = Merger.whererere(branchName, brnaches, main);
        File splitFile = join(COMMIT_FOLDER, splitHash);
        Commit splitCommit = Utils.readObject(splitFile, Commit.class);
        boolean conflict = false;

        File searchFile = join(COMMIT_FOLDER, brnaches.get(branchName));
        Commit searchCommit = Utils.readObject(searchFile, Commit.class);
        String mergeMessage = ("Merged " + branchName + " into " + curBranch + ".");
        Commit mergeCommit = new Commit(mergeMessage, curHead, new HashMap<>());
        mergeCommit.setSecondParent(brnaches.get(branchName));

        conflict = Merger.doublecheker(splitCommit, conflict, searchCommit, mergeCommit, main);

        for (String i : main.getCommitObject().keySet()) {
            conflict = Merger.thirdCase(splitCommit, conflict, searchCommit, mergeCommit, i, main);
        }
        for (String i : searchCommit.getCommitObject().keySet()) {
            Merger.fourthCase(splitCommit, searchCommit, mergeCommit, i, main);
        }
        main = mergeCommit;
        curHead = Utils.sha1(mergeCommit.getList());
        allofthe.add(curHead);
        makeFile(mergeCommit, curHead);
        splitHash = curHead;
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }

        stagedFiles.clear();
        plzwork.clear();
        Utils.writeContents(REPOSITORY, curHead);
        Utils.writeObject(CURR_COMMIT, allofthe);
        Utils.writeContents(SPLITEE, splitHash);
        Utils.writeObject(HOLDEROFMAIN, main);
        Utils.writeObject(ALLBRANCHES, brnaches);
        Utils.writeObject(stager, stagedFiles);
        Utils.writeObject(REMOVEFILE, plzwork);
    }

}
