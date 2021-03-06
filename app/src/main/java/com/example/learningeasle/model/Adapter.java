package com.example.learningeasle.model;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learningeasle.PostDetailActivity;
import com.example.learningeasle.MainFragments.PostFragment;
import com.example.learningeasle.R;
import com.example.learningeasle.ViewAttachement;
import com.example.learningeasle.ViewImage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Adapter extends RecyclerView.Adapter<Adapter.PostHolder> {
    Context context;
    List<modelpost> postList;
    private String pName;
    private DatabaseReference likesRef;
    DatabaseReference postsref;
    String myId;
    boolean processLike=false;
    private EditClick edit;
    View view;
    String audiourl, pdfurl,videourl;
    public Adapter(Context context, List<modelpost> postList,EditClick editClick) {
        this.context = context;
        this.postList = postList;
        this.edit = editClick;
        myId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef= FirebaseDatabase.getInstance().getReference().child("Likes");
        postsref= FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public Adapter.PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(context).inflate(R.layout.row_post, parent, false);
        return new PostHolder(view,edit);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostHolder holder, final int position) {
        final String uName=postList.get(position).getpName();
        final String url=postList.get(position).getuImage();
        final String pTitle = postList.get(position).getpTitle();
        final String pDescription = postList.get(position).getpDesc();
        final String pType=postList.get(position).getpType();
        final String pImage = postList.get(position).getpImage();
        final String pTimeStamp = postList.get(position).getpTime();
        final String pId = postList.get(position).getpId();
        final String pLikes=postList.get(position).getpLikes();
        String pComments=postList.get(position).getpComments();
        videourl = postList.get(position).getVideourl();
        audiourl = postList.get(position).getAudiourl();
        pdfurl = postList.get(position).getPdfurl();
        final String[] viewsCount = new String[1];

        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Views");
        databaseReference.child(pTimeStamp).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                viewsCount[0] =snapshot.getValue().toString();
                int viewsCnt = Integer.parseInt(viewsCount[0]);
                System.out.println(viewsCnt + "= views");
                viewsCnt++;

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Views");
                ref.child(pTimeStamp).setValue(Integer.toString(viewsCnt));
                holder.views.setText(viewsCount[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //If any of the url is non empty make attachement btn visible
        if(!videourl.equals("empty")||!(audiourl.equals("empty"))||!(pdfurl.equals("empty"))){
            holder.attachement.setVisibility(View.VISIBLE);
        }

        //when attached floating button is clicked make visible all those floting button whose value is not empty
        holder.attachement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videourl = postList.get(position).getVideourl();
                pdfurl = postList.get(position).getPdfurl();
                audiourl = postList.get(position).getAudiourl();
                if(!videourl.equals("empty")){
                    holder.video_btn.setVisibility(View.VISIBLE);

                }
                if(!pdfurl.equals("empty")){
                    holder.pdf_btn.setVisibility(View.VISIBLE);
                }
                if(!audiourl.equals("empty")){
                    holder.audio_btn.setVisibility(View.VISIBLE);
                }
            }
        });

        //Pass the url of the attached file which user want to view
        holder.video_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewAttachement.class);
                intent.putExtra("videourl",videourl);
                intent.putExtra("audiourl","empty");
                intent.putExtra("pdfurl","empty");
                context.startActivity(intent);
            }
        });
        holder.audio_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewAttachement.class);
                intent.putExtra("videourl","empty");
                intent.putExtra("audiourl",audiourl);
                intent.putExtra("pdfurl","empty");
                context.startActivity(intent);
            }
        });
        holder.pdf_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewAttachement.class);
                intent.putExtra("videourl","empty");
                intent.putExtra("audiourl","empty");
                intent.putExtra("pdfurl",pdfurl);
                context.startActivity(intent);
            }
        });

        PostFragment postFragment = new PostFragment();
        Bundle bundle = new Bundle();
        bundle.putString("Id",pId);
        bundle.putString("pTime",pTimeStamp);
        bundle.putString("Edit","EditPost");
        bundle.putString("Title",pTitle);
        bundle.putString("Des",pDescription);
        bundle.putString("Url",pImage);
        bundle.putString("Likes",pLikes);
        bundle.putString("Comments",pComments);
        postFragment.setArguments(bundle);

        //Set the userdetails for the post
        holder.pName.setText(uName);
        holder.pType.setText(pType);
        if(url.equals("empty"))
            holder.url.setImageResource(R.drawable.ic_action_account);
        else
            Picasso.get().load(url).into(holder.url);
        System.out.println(pDescription+"  ..  "+pImage);
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));

        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();



        //Setting the comment count of the post
        if(pComments==null)
            pComments="0";
        holder.pTotalComment.setText(pComments + " Comments");
        //Setting post image
        if (pImage.equals("noImage")){
            System.out.println(pTitle+"  . "+pDescription);
            holder.Image.setVisibility(View.GONE);
        } else{
            try {
                holder.Image.setVisibility(View.VISIBLE);
                Picasso.get().load(pImage).placeholder(R.drawable.ic_default).fit().centerCrop().into(holder.Image);
            }catch (Exception e){}
        }

        //Setting the post details
        holder.pTime.setText(pTime);
        holder.pTitle.setText(pTitle);
        holder.pDesc.setText(pDescription);
        holder.pTotalLikes.setText(pLikes +" Likes");

        setLikes(holder,pTimeStamp);
        //Liked btn is clicked if post is not liked then make it liked nad viceversa
        holder.like_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(context, "Like", Toast.LENGTH_SHORT).show();
                final int pLikes;
                String likes = postList.get(position).getpLikes();
                if (likes == null)
                    pLikes = 0;
                else
                    pLikes = Integer.parseInt(postList.get(position).getpLikes());
                processLike = true;
                final String stamp = postList.get(position).getpTime();
                postsref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (processLike)
                            if (snapshot.child(stamp).child("Likes").hasChild(myId)) {
                                postsref.child(stamp).child("pLikes").setValue("" + (pLikes - 1));
                                postsref.child(stamp).child("Likes").child(myId).removeValue();
                                processLike = false;
                            } else {

                                postsref.child(stamp).child("pLikes").setValue("" + (pLikes + 1));
                                postsref.child(stamp).child("Likes").child(myId).setValue("Liked");
                                processLike = false;
                            }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });
        //User want to comment on the post
        holder.comment_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId",pTimeStamp);
                context.startActivity(intent);
            }
        });
        //Share the post
        holder.share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BitmapDrawable bitmapDrawable= (BitmapDrawable) holder.Image.getDrawable();
                if(bitmapDrawable == null){
                    //Post has no image share only text
                    shareTextOnly(pTitle,pDescription);
                }
                else{
                    //Share image too
                    Bitmap bitmap=bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle,pDescription,bitmap);
                }
            }
        });

        //Image is clicked show user fullview of image
        holder.Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, ViewImage.class);
                intent.putExtra("image",pImage);
                context.startActivity(intent);

            }
        });

        //Setting the bookmark on those post whch are bookmarked
        setBookmark(holder, myId, pId, pTimeStamp);
        //Bookmark the post if its not bookmarked and vice-versa
        holder.bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(myId).child("Bookmarks");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(pTimeStamp)) {
                            reference.child(pTimeStamp).removeValue();
                            holder.bookmark.setImageResource(R.drawable.ic_baseline_bookmark_border_24);
                        } else {
                            reference.child(pTimeStamp).setValue(pId);
                            //holder.boookmark.setImageDrawable(context.getResources().getDrawable(R.drawable.bookmarked));
                            holder.bookmark.setImageResource(R.drawable.bookmarked);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    private void setBookmark(final PostHolder holder, String myId, String pId, final String pTimeStamp) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(myId);//.child("Bookmarks");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("Bookmarks").hasChild(pTimeStamp)){
                    holder.bookmark.setImageDrawable(context.getResources().getDrawable(R.drawable.bookmarked));
                }else{

                    holder.bookmark.setImageResource(R.drawable.ic_baseline_bookmark_border_24);
                    //holder.boookmark.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_bookmark_border_24));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //After deleting the post from bookmark section then delete the post and its data from the storage section
    private void beginDelete(final String pId, String pImage, final String pTimeStamp,String videourl,String audiourl,String pdfurl) {

        //First of all Delete the attachements from the storage if their and also the view count of the post from realtime database
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Views");
        databaseReference.child(pTimeStamp).removeValue();
        StorageReference reference = FirebaseStorage.getInstance().getReference();
        //Deleting the file attached with the post if any
        if(!videourl.equals("empty")){
            StorageReference videoref = FirebaseStorage.getInstance().getReferenceFromUrl(videourl);
            videoref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(context,"Attached Video File Deleted Successfully",Toast.LENGTH_SHORT).show();
                }
            });

        }
        if(!audiourl.equals("empty")){

            StorageReference audioref = FirebaseStorage.getInstance().getReferenceFromUrl(audiourl);
            audioref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(context,"Attached Audio File Deleted Successfully",Toast.LENGTH_SHORT).show();
                }
            });
        }
        if(!pdfurl.equals("empty")){
            StorageReference pdfref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfurl);
            pdfref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(context,"Attached Pdf File Deleted Successfully",Toast.LENGTH_SHORT).show();
                }
            });
        }
        //Begin delete accordingly if post contain the image than first delete the image from the storage section
        //then delete the post from the realtime database;
        if(pImage.equals("noImage")){
            final ProgressDialog pd = new ProgressDialog(context);
            pd.setMessage("Deleting....");
            //If there in no image than we need to delete this only from the realtime database
            Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot ds:snapshot.getChildren()){
                        HashMap<String,Object> hashMap = (HashMap<String, Object>) ds.getValue();
                        if (hashMap.get("pTime").equals(pTimeStamp)) {
                            ds.getRef().removeValue();
                            Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                        }

                    }

                    pd.dismiss();


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else{
            //If image is present than delete it from the storage too
            final ProgressDialog pd = new ProgressDialog(context);
            pd.setMessage("Deleting....");

            StorageReference picref = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
            picref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot ds:snapshot.getChildren()){
                                HashMap<String,Object> hashMap = (HashMap<String, Object>) ds.getValue();
                                if(hashMap.get("pTime").equals(pTimeStamp))
                                    ds.getRef().removeValue();
                                Toast.makeText(context,"Deleted Successfully",Toast.LENGTH_SHORT).show();
                            }
                            pd.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    //Toast.makeText(context,"Unable to delete Post",Toast.LENGTH_SHORT).show();
                }
            });
        }
        //Setting the bookmark on those post whch are bookmarked

    }

    //Delete the post from the bookmark section of all the users when user this post
    private void deletefromBookmarks(final String pTimeStamp, final String pImage, final String pId, final String videourl, final String audiourl, final String pdfurl) {
        final DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    String path = ds.getKey();
                    if(ds.child("Bookmarks").hasChild(pTimeStamp)){
                        ref.child(path).child("Bookmarks").child(pTimeStamp).removeValue();
                        beginDelete(pId,pImage,pTimeStamp,videourl,audiourl,pdfurl);
                    }
                    else {
                        beginDelete(pId,pImage,pTimeStamp,videourl,audiourl,pdfurl);
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {
        String shareBody=pTitle+"\n"+pDescription;
        Uri uri=saveImageInCache(bitmap);
        Intent shareIntent=new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM,uri);
        shareIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
        shareIntent.setType("image/png");
        context.startActivity(Intent.createChooser(shareIntent,"Share Via"));
    }

    private Uri saveImageInCache(Bitmap bitmap) {
        File imageFolder=new File(context.getCacheDir(),"images");
        Uri uri=null;
        try{
            imageFolder.mkdirs();
            File file=new File(imageFolder,"shared_image.png");
            FileOutputStream stream=new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,stream);
            stream.flush();
            stream.close();
            uri= FileProvider.getUriForFile(context,"com.example.learningeasle.fileprovider",file);
        }catch (Exception e){
            e.printStackTrace();
        }
        return uri;
    }

    private void shareTextOnly(String pTitle, String pDescription) {
        String shareBody=pTitle+"\n"+pDescription;
        Intent shareIntent=new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here"); // for sharing via email
        shareIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
        context.startActivity(Intent.createChooser(shareIntent,"Share Via"));
    }

    //Setting the liked btn that user has liked this particular post or not
    private void setLikes(final PostHolder holder, final String pTimeStamp) {
        postsref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child(pTimeStamp).hasChild("Likes") && snapshot.child(pTimeStamp).child("Likes").hasChild(myId)) {
//                        System.out.println(ds.child("Likes")+".........."+myId);
                    holder.like_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favourite_border, 0, 0,
                            0);
                    holder.like_btn.setText("Liked");
                }
                else {
                    holder.like_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favourite, 0, 0,
                            0);
                    holder.like_btn.setText("Like");
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class PostHolder extends RecyclerView.ViewHolder {

        ImageView url,Image,bookmark;
        TextView pName, pTime, pTitle, pDesc, pTotalLikes,pTotalComment,pType,views;
        ImageButton morebtn;
        Button like_btn, share_btn, comment_btn;
        EditClick editClick;
        FloatingActionButton attachement,audio_btn,video_btn,pdf_btn;
        public PostHolder(@NonNull View itemView, final EditClick editClick) {
            super(itemView);
            url=itemView.findViewById(R.id.uDp);
            Image = itemView.findViewById(R.id.pImage);
            pName=itemView.findViewById(R.id.uname);
            pTime = itemView.findViewById(R.id.time);
            pTitle = itemView.findViewById(R.id.ptitle);
            pTotalComment=itemView.findViewById(R.id.totalcomments);
            pDesc = itemView.findViewById(R.id.pdesc);
            pTotalLikes = itemView.findViewById(R.id.totallikes);
            morebtn = (ImageButton) itemView.findViewById(R.id.more);
            like_btn = itemView.findViewById(R.id.like);
            share_btn = itemView.findViewById(R.id.share);
            comment_btn = itemView.findViewById(R.id.comment);
            pType=itemView.findViewById(R.id.pType);
            views=itemView.findViewById(R.id.viewCount);
            attachement = itemView.findViewById(R.id.view_attached);
            audio_btn = itemView.findViewById(R.id.audio_upload);
            video_btn = itemView.findViewById(R.id.video_upload);
            pdf_btn = itemView.findViewById(R.id.pdf_upload);
            bookmark = itemView.findViewById(R.id.bookmarks);
            this.editClick = editClick;
            //More btn is clicked by user show him dialog to edit or delete the post
            morebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final String[] options = {"Edit", "Delete","Cancel"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setItems(options,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (options[i].equals("Edit")) {
                                //Edit btn is clicked call the editClick method of the interface EditClick
                                if (getAdapterPosition() != -1) {
                                    editClick.onEditClick(getAdapterPosition(), postList.get(getAdapterPosition()).pId, postList.get(getAdapterPosition()).pTime, "EditPost", postList.get(getAdapterPosition()).pTitle, postList.get(getAdapterPosition()).pDesc, postList.get(getAdapterPosition()).pImage,
                                            postList.get(getAdapterPosition()).pLikes, postList.get(getAdapterPosition()).pComments);
                                }
                            }

                            if (options[i].equals("Delete")) {
                                final String time = postList.get(getAdapterPosition()).pTime;
                                final String pImage = postList.get(getAdapterPosition()).pImage;
                                final String pId = postList.get(getAdapterPosition()).pId;
                                final String videourl = postList.get(getAdapterPosition()).getVideourl();
                                final String audiourl = postList.get(getAdapterPosition()).getAudiourl();
                                final String pdfurl = postList.get(getAdapterPosition()).getPdfurl();
                                //Show user an alert dialog that he surely want to delete the post
                                AlertDialog.Builder delete = new AlertDialog.Builder(context);
                                delete.setTitle("Are You Sure?");
                                delete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Before deleting the post delete it from the bookmarks section of the users;
                                        deletefromBookmarks(time,pImage,pId,videourl,audiourl,pdfurl);
                                    }
                                });

                                delete.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Close Dialog
                                    }
                                });
                                AlertDialog alert = delete.create();
                                alert.show();
                                //    Customising buttons for dialog

                            }
                        }
                    });
                    builder.create().show();

                }
            });
        }
    }


    //Interface to pass the data from the post to the post fragment to set the image text and des of the post to be edited
    public interface  EditClick{
        public void onEditClick(int position,String Uid,String pTimeStamp,String post,String title,String content,String url,String pLikes,String pComment);
    }

}