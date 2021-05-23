package com.example.uploaddocuments;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Attachment extends DialogFragment {

    private static final String ARG_IMG_URIS = "uri-list";
    private static final String HIDE_BUTTON = "hide_button";
    private List<String> attachments;
    private List<Uri> uri;
    private boolean hideDeleteButton;
    private AttachmentListAdapter attachmentListAdapter;
    private static AttachmentDeleteListener listener;

    public Attachment() {
    }

    public Attachment(AttachmentDeleteListener listener) {
        Attachment.listener = listener;
    }

    public static @org.jetbrains.annotations.Nullable Attachment newInstance(@org.jetbrains.annotations.Nullable List<String> uris, boolean hideDeleteButton, @NotNull List<Uri> mUris) {
        Attachment fragment = new Attachment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_IMG_URIS, (ArrayList<String>) uris);
        args.putParcelableArrayList("uris", (ArrayList<? extends Parcelable>) mUris);
        args.putBoolean(HIDE_BUTTON, hideDeleteButton);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        } else {
            setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_NoActionBar);
        }
        if (getArguments() != null) {
            if (getArguments().containsKey(ARG_IMG_URIS))
                attachments = getArguments().getStringArrayList(ARG_IMG_URIS);
            uri = getArguments().getParcelableArrayList("uris");
            if (getArguments().containsKey(HIDE_BUTTON))
                hideDeleteButton = getArguments().getBoolean(HIDE_BUTTON);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_attachment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.rec_attachment_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        attachmentListAdapter = new AttachmentListAdapter(this::deleteItem, false, getContext(),uri);
        recyclerView.setAdapter(attachmentListAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    // Do something
                } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    // Do something
                } else {
                    // Do something
                    if (attachments != null && attachments.get(0).contains("storage") && attachments.get(0).endsWith("pdf"))
                        attachmentListAdapter.notifyItemRangeChanged(0, attachments.size());

                }
            }

        });


        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        if (attachments != null) {
            attachmentListAdapter.setAttachmentsList(attachments,uri);
        }


    }

    private void deleteItem(String url) {
        confirmPrescriptionDeletion(url);
    }

    private void confirmPrescriptionDeletion(String url) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(requireContext());
        builder1.setMessage("Are you sure you wan to delete this attachment?");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Yes",
                (dialog, id) -> {
                    if (listener != null)
                        listener.onDeleteClick(url);
                    attachments.remove(url);
                    if (attachments.size() == 0) dismiss();
                    attachmentListAdapter.notifyDataSetChanged();
                    dialog.cancel();
                    //dismiss();
                });

        builder1.setNegativeButton(
                "No",
                (dialog, id) -> {
                    dialog.cancel();
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    public interface AttachmentDeleteListener {
        void onDeleteClick(String url);
    }

}
