package com.himanshu.infirment.fragment;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.himanshu.infirment.R;
import com.himanshu.infirment.adapter.VideoAdapter;
import com.himanshu.infirment.model.VideoItem;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class VideoFragment extends Fragment {

    private List<VideoItem> videoData;
    private VideoAdapter videoAdapter;
    private FirebaseAuth auth;
    public static final int PICK_VIDEO = 1;
    private Uri videoUri;
    private StorageReference storageReference;
    private DatabaseReference videoReference;
    private VideoItem videoItem;
    private UploadTask uploadTask;
   private FloatingActionButton floatingActionButton;
    private RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        videoReference = FirebaseDatabase.getInstance().getReference().child("Videos");
        storageReference = FirebaseStorage.getInstance().getReference().child("Videos");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        floatingActionButton = view.findViewById(R.id.add_video);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadVideo();
            }
        });

        recyclerView = view.findViewById(R.id.video_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        videoData = new ArrayList<>();


        videoItem = new VideoItem();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recyclerViewShowAllData();
    }

    //For Load RecyclerView Data
    public void recyclerViewShowAllData() {

        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_progress);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        videoReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot npsnapshot : snapshot.getChildren()) {
                        VideoItem item = npsnapshot.getValue(VideoItem.class);
                        videoData.add(item);
                    }
                    videoAdapter = new VideoAdapter(videoData);
                    recyclerView.setAdapter(videoAdapter);
                }
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //For Upload Video
    public void uploadVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_VIDEO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO || resultCode == RESULT_OK
                || data != null || data.getData() != null) {
            videoUri = data.getData();
            storeUploadedImageInfo();
        }
    }

    private String getExt(Uri uri) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void storeUploadedImageInfo() {

        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_progress);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();


        final StorageReference reference = storageReference
                .child(System.currentTimeMillis() + "." + getExt(videoUri));
        uploadTask = reference.putFile(videoUri);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return reference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUrl = task.getResult();
                    dialog.dismiss();
                    Toast.makeText(getActivity(), "Uploaded Successfully", Toast.LENGTH_SHORT).show();

                    videoItem.setUrl(downloadUrl.toString());
                    videoItem.setTime(System.currentTimeMillis() + "");
                    final String id = videoReference.push().getKey();
                    videoReference.child(id).setValue(videoItem);
                } else {
                    Toast.makeText(getActivity(), "Failed : " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}