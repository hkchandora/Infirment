package com.himanshu.infirment.adapter;


import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.himanshu.infirment.R;
import com.himanshu.infirment.SignUpActivity;
import com.himanshu.infirment.model.VideoItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {
    private List<VideoItem> videoData;
    private Context context;

    public VideoAdapter(List<VideoItem> videoData) {
        this.videoData = videoData;
    }

    public VideoAdapter(List<VideoItem> videoData, Context context) {
        this.videoData = videoData;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int i) {

        final VideoItem ld = videoData.get(i);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date resultDate = new Date(Long.parseLong(ld.getTime()));
        holder.date.setText(sdf.format(resultDate));

        Glide.with(holder.image.getContext()).asBitmap().load(ld.getUrl()).placeholder(R.drawable.ic_video).into(holder.image);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Download")
                        .setMessage("Are you want to download this video?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String url = ld.getUrl().trim();

                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                                        .setTitle("Downloading")
                                        .setDescription("Video Downloading")
                                        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                        .setAllowedOverMetered(true)
                                        .setAllowedOverRoaming(true);

                                DownloadManager manager = (DownloadManager) v.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                                manager.enqueue(request);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView date;
        public ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.uploaded_txt);
            image = itemView.findViewById(R.id.uploaded_img);
        }
    }
}
