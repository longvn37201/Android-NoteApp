package com.vulong.noteapp.adapter;

import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.vulong.noteapp.R;
import com.vulong.noteapp.entities.Note;
import com.vulong.noteapp.listeners.NoteAdapterListener;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> implements Filterable {
    private static final String TAG = "test";



    private ArrayList<Note> noteListFiltered;
    private ArrayList<Note> noteListSource;

    private NoteAdapterListener noteAdapterListener;

    public NoteAdapter(ArrayList<Note> noteList, NoteAdapterListener noteAdapterListener) {
        this.noteAdapterListener = noteAdapterListener;
        this.noteListFiltered = noteList;
        this.noteListSource = noteList;

    }

    public void setNoteListSource(ArrayList<Note> noteList) {

        this.noteListSource = noteList;
    }
    public ArrayList<Note> getNoteListFiltered() {
        return noteListFiltered;
    }

    public void setNoteListFiltered(ArrayList<Note> noteListFiltered) {
        this.noteListFiltered = noteListFiltered;
    }

    public ArrayList<Note> getNoteListSource() {
        return noteListSource;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_note, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Note note = noteListFiltered.get(position);
        holder.setNote(note);

        holder.constraintLayout.setOnClickListener(v -> {
            noteAdapterListener.onNoteSelected(position, noteListFiltered.get(position));
        });




    }

    @Override
    public int getItemCount() {
        return noteListFiltered.size();
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.trim().isEmpty()) {
                    noteListFiltered = noteListSource;
                } else {
                    ArrayList<Note> filteredList = new ArrayList<>();
                    for (Note note : noteListSource) {

                        if (note.getTitle().toLowerCase().contains(charString.toLowerCase()) || note.getContent().contains(constraint)) {
                            filteredList.add(note);
                        }
                    }

                    noteListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = noteListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                noteListFiltered = (ArrayList<Note>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvContent;
        private TextView tvDateTime;
        private ConstraintLayout constraintLayout;
        private RoundedImageView imgImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvDateTime = itemView.findViewById(R.id.tv_date_time_in_item);
            constraintLayout = itemView.findViewById(R.id.layout_item);
            imgImage = itemView.findViewById(R.id.img_rounded);
        }

        private void setNote(Note note) {
            tvTitle.setText(note.getTitle());
            tvContent.setText(note.getContent());
            tvDateTime.setText(note.getDateTime());
//        constraintLayout.setBackgroundT(note.getColor());
            constraintLayout.getBackground().setColorFilter(note.getColor(), PorterDuff.Mode.SRC_ATOP);


            try {
                imgImage.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
            } catch (Exception e) {
                //lost path
                imgImage.setVisibility(View.GONE);
            }
        }
    }




}

