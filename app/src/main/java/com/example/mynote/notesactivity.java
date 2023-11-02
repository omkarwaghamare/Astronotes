package com.example.mynote;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class notesactivity extends AppCompatActivity {


    FloatingActionButton mcreatenotefab;
    ImageButton menubtn;
    FirebaseAuth firebaseAuth;
    //    Button button;
    RecyclerView mreyclerview;
    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;
    FirestoreRecyclerAdapter<firebasemodel, NoteViewHolder> noteAdapter;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notesactivity);

//        button = findViewById(R.id.button1);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FirebaseAuth.getInstance().signOut();
//                Intent intent = new Intent(notesactivity.this, MainActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        });

        mcreatenotefab = findViewById(R.id.createnotefab);
        firebaseAuth = FirebaseAuth.getInstance();
        menubtn = findViewById(R.id.menu_icon);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        //  getSupportActionBar().setTitle("All Notes");
        menubtn.setOnClickListener(v -> showMenu());


        mcreatenotefab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(notesactivity.this, createnote.class));

            }
        });

        Query query = firebaseFirestore.collection("notes").document(firebaseUser.getUid()).collection("myNotes").orderBy("title", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<firebasemodel> allusernotes = new FirestoreRecyclerOptions.Builder<firebasemodel>()
                .setQuery(query, firebasemodel.class)
                .build();

        noteAdapter = new FirestoreRecyclerAdapter<firebasemodel, NoteViewHolder>(allusernotes) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull firebasemodel model) {


                ImageView popupbutton = noteViewHolder.itemView.findViewById(R.id.menupopbutton);

                int colourcode = getRandomColor();
                noteViewHolder.mnote.setBackgroundColor(noteViewHolder.itemView.getResources().getColor(colourcode));
                noteViewHolder.notetitle.setText(model.getTitle());
                noteViewHolder.notecontent.setText(model.getContent());
                String docId = noteAdapter.getSnapshots().getSnapshot(i).getId();

                noteViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        // we have to open note detail activity
                        Intent intent = new Intent(v.getContext(), notedetails.class);
                        intent.putExtra("title", model.getTitle());
                        intent.putExtra("content", model.getTitle());
                        intent.putExtra("noteId", docId);
                        v.getContext().startActivity(intent);
//                        Toast.makeText(getApplicationContext(), "This is clicked", Toast.LENGTH_SHORT).show();
                    }
                });


                popupbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                        popupMenu.setGravity(Gravity.END);
                        popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(@NonNull MenuItem item) {
                                Intent intent = new Intent(v.getContext(), editnoteactivity.class);
                                intent.putExtra("title", model.getTitle());
                                intent.putExtra("content", model.getContent());
                                intent.putExtra("noteId", docId);
                                v.getContext().startActivity(intent);
                                return false;
                            }
                        });

                        popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(@NonNull MenuItem item) {
                                //  Toast.makeText(getApplicationContext(), "This is note is deleted", Toast.LENGTH_SHORT).show();
                                DocumentReference documentReference = firebaseFirestore.collection("notes").document(firebaseUser.getUid()).collection("myNotes").document(docId);
                                documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(getApplicationContext(), "This is note is deleted", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return false;
                            }
                        });
                        popupMenu.show();
                    }
                });
            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_layout, parent, false);
                return new NoteViewHolder(view);
            }
        };

        mreyclerview = findViewById(R.id.recycleview);
        mreyclerview.setHasFixedSize(true);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mreyclerview.setLayoutManager(staggeredGridLayoutManager);
        mreyclerview.setAdapter(noteAdapter);

    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView notetitle;
        private final TextView notecontent;
        LinearLayout mnote;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            notetitle = itemView.findViewById(R.id.notetitle);
            notecontent = itemView.findViewById(R.id.notecontent);
            mnote = itemView.findViewById(R.id.note);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }

    protected void onStop() {
        super.onStop();
        if (noteAdapter != null) {
            noteAdapter.stopListening();
        }

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu, menu);
//        return true;
//    }
//
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == R.id.logoutb) {
//            // Handle the logout action here
//            firebaseAuth.signOut();
//            finish();
//            startActivity(new Intent(notesactivity.this, MainActivity.class));
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private int getRandomColor() {
        List<Integer> colorcode = new ArrayList<>();
        colorcode.add(R.color.gray);
        colorcode.add(R.color.green);
        colorcode.add(R.color.pink);
        colorcode.add(R.color.col3);
        colorcode.add(R.color.col1);
        colorcode.add(R.color.col2);
        colorcode.add(R.color.col4);
        colorcode.add(R.color.col5);
        colorcode.add(R.color.lightgreen);
        colorcode.add(R.color.skyblue);

        Random random = new Random();
        int number = random.nextInt(colorcode.size());
        return colorcode.get(number);

    }

    void showMenu() {
        PopupMenu popupMenu = new PopupMenu(notesactivity.this, menubtn);
        popupMenu.getMenu().add("Logout");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle() == "Logout") {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(notesactivity.this, MainActivity.class));
                    finish();
                    return true;
                }
                return false;
            }
        });

    }
}
