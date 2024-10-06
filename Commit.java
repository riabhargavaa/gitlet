package gitlet;

// TODO: any imports you need here

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;
import java.util.List;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    /** Mapping the all the files made by this commit */
    private HashMap<String, byte[]> CommitObject;
    /** List of all the parents */
    private List<String> parents;
    private String id;
    //gets parent
    private String parent;
    private Date date;
    private String secondParent;


    public void Commit() {
        this.parents = new ArrayList<>();
        message = "initial commit";
        date = new Date(0);
        CommitObject = new HashMap<String, byte[]>();
        this.id = getID();
    }
    public Commit(String mess, String curpar, HashMap<String, byte[]> direc) {
        message = mess;
        parent = curpar;
        CommitObject = direc;
        if(curpar == null) {
            //inital date
            date = new Date(0);
        } else {
            date = new Date();
        }
    }

    public String getParent() {
        return parent;
    }

    public String getSecondParent() {
        return secondParent;
    }

    public String getDate() {
        SimpleDateFormat correctFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        return correctFormat.format(this.date);
    }
    public List<Object> getList() {
        List<Object> list = new ArrayList<>();
        list.add(message);
        //not inital commit
        if(parent != null) {
            list.add(parent);
        }
        list.add(getDate());
        return list;
    }
    public void addCommit(String x , byte[] y) {
        CommitObject.put(x,y);
    }
    public void setSecondParent(String s) {
        secondParent = s;
    }
    public HashMap<String, byte[]> getCommitObject() {
        return CommitObject;
    }

    public String getMessage() {
        return message;
    }

    public String getID() {
        return Utils.sha1(this.message, getTimestamp(), this.parents.toString(), this.CommitObject.toString());
    }

    private Object getTimestamp() {
        SimpleDateFormat correctFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        return correctFormat.format(date);
    }

    public void Commit(HashMap<String, byte[]> ob, String mess) {
        message = mess;
        CommitObject = ob;
    }




    /* TODO: fill in the rest of this class. */
}