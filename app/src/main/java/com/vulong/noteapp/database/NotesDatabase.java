package com.vulong.noteapp.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import com.vulong.noteapp.DAO.NoteDAO;
import com.vulong.noteapp.entities.Note;

@Database(entities = Note.class, version = 1,exportSchema = false)
public abstract class NotesDatabase extends RoomDatabase {

    //singleton
    private static NotesDatabase notesDatabase;

    public static synchronized NotesDatabase getInstance(Context context) {
        if(notesDatabase==null){
            notesDatabase= Room.databaseBuilder(
                    context,
                    NotesDatabase.class,
                    "note_app_db"
            ).build();
        }
        return  notesDatabase;
    }

    public abstract NoteDAO noteDAO();

}
