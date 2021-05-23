package com.example.uploaddocuments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.uploaddocuments.databinding.PdfViewItemBinding;
import com.example.uploaddocuments.databinding.ViewAttachmentItemBinding;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import okhttp3.Call;

import com.squareup.picasso.BuildConfig;
import com.squareup.picasso.Callback;
import okhttp3.Response;

public class AttachmentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int LIST_ITEM_IMAGE = 1;
    public static final int LIST_ITEM_PDF = 2;
    public static final int LIST_ITEM_DOC = 3;

    private List<String> attachementUrls;
    private List<Uri> uriList;
    private AttachmentListener listener;
    private boolean hideDeleteButton;
    public static Context context;

    public AttachmentListAdapter(AttachmentListener listener, boolean hideDeleteButton, Context context, List<Uri> uri) {
        this.listener = listener;
        this.hideDeleteButton = hideDeleteButton;
        this.context = context;
        uriList= uri;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater from = LayoutInflater.from(parent.getContext());
        if (viewType == LIST_ITEM_IMAGE) {
            return new ListItemImageViewHolder(ViewAttachmentItemBinding.inflate(from, parent, false));
        } else {
            return new ListItemPdfViewHolder(PdfViewItemBinding.inflate(from, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ListItemImageViewHolder) {
            ((ListItemImageViewHolder) holder).bind(attachementUrls.get(position), listener, hideDeleteButton);
        } else if (holder instanceof ListItemPdfViewHolder) {
            ((ListItemPdfViewHolder) holder).bind(attachementUrls.get(position), listener, hideDeleteButton,uriList.get(position));
        }
    }


    @Override
    public int getItemViewType(int position) {
        String prescription = attachementUrls.get(position);
        if (prescription != null && (prescription.endsWith(".pdf") || prescription.endsWith(".doc") || prescription.endsWith(".docx"))) {
            return LIST_ITEM_PDF;
        } else {
            return LIST_ITEM_IMAGE;
        }
    }


    public void setAttachmentsList(List<String> newAttachementUrls, List<Uri> uri) {
        this.attachementUrls = newAttachementUrls;
        this.uriList = uri;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return attachementUrls.size();
    }


    static class ListItemImageViewHolder extends RecyclerView.ViewHolder {
        public ListItemImageViewHolder(@NonNull View itemView) {
            super(itemView);
        }


        ViewAttachmentItemBinding binding;
        ListItemImageViewHolder(ViewAttachmentItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }


        void bind(String prescription, AttachmentListener listener, boolean hideDeleteButton) {
            binding.setListener(listener);

            if (!prescription.startsWith("http")) {
                Uri uri = Uri.parse(prescription);
                File imgFile = new File(uri.getPath());
                if (imgFile.exists()) {
                    Glide.with(context).load(imgFile).into(binding.attachmentLocalImage);
                    binding.attachmentLocalImage.setVisibility(View.VISIBLE);
                    binding.attachment.setVisibility(View.GONE);
                    binding.setUrl(prescription);
                    binding.couldNotLoad.setVisibility(View.GONE);
                }

                binding.progressBar.setVisibility(View.GONE);
            } else {
                binding.setUrl(prescription);
                binding.attachmentLocalImage.setVisibility(View.GONE);
                binding.attachment.setVisibility(View.VISIBLE);
            }

            if (hideDeleteButton)
                binding.deleteAttachmentButton.setVisibility(View.GONE);
            else binding.deleteAttachmentButton.setVisibility(View.VISIBLE);

            binding.setImageLoadingCallback(new Callback() {
                @Override
                public void onSuccess() {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.couldNotLoad.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    binding.progressBar.setVisibility(View.GONE);
                }
            });
            binding.executePendingBindings();
        }
    }

    static class ListItemPdfViewHolder extends RecyclerView.ViewHolder {

        private PdfViewItemBinding binding;

        public ListItemPdfViewHolder(PdfViewItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(String prescription, AttachmentListener listener, boolean hideDeleteButton, Uri uri) {
            if (hideDeleteButton) binding.deletePdf.setVisibility(View.GONE);
            else binding.deletePdf.setVisibility(View.VISIBLE);

            binding.setListener(listener);

            if (prescription.contains(BuildConfig.APPLICATION_ID)) {

                if (prescription.endsWith(".doc") || prescription.endsWith(".docx")) {
                    binding.pdfView.setBackground(context.getResources().getDrawable(R.drawable.ic_baseline_article_24));
                    binding.pdfView.setOnClickListener(v -> {

                        Intent target = new Intent(Intent.ACTION_VIEW);
                        target.setDataAndType(uri, "application/msword");
                        //   target.setDataAndType(Uri.parse(prescription), "application/msword");
                        //target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        target.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Intent intent = Intent.createChooser(target, "Open File");
                        try {
                            context.startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(context,"Your device doesn't have Word viewer ", Toast.LENGTH_LONG).show();
                        }

                      /*  Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        context.startActivity(Intent.createChooser(intent, "ChooseFile"));*/
                    });
                }
                else{
                    binding.pdfView.setBackground(context.getResources().getDrawable(R.drawable.ic_pdf));

                    binding.pdfView.setOnClickListener(v -> {

                        Intent intent =new  Intent(context, WebViewActivity.class);
                        intent.putExtra("fileUrl",prescription);
                        context.startActivity(intent);
                    });

                }
                binding.webView.setVisibility(View.GONE);
                binding.pdfView.setVisibility(View.VISIBLE);
                // binding.pdfView.fromFile(prescription).show();
                binding.progressBar2.setVisibility(View.GONE);
                binding.setUrl(prescription);





            } else {
                binding.webView.setVisibility(View.VISIBLE);
                binding.pdfView.setVisibility(View.GONE);
                WebSettings webSettings = binding.webView.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webSettings.setBuiltInZoomControls(true);
                webSettings.setAllowFileAccessFromFileURLs(true);
                webSettings.setAllowUniversalAccessFromFileURLs(true);
                webSettings.setDomStorageEnabled(true);
                webSettings.setDatabaseEnabled(true);

                try {
                    binding.setUrl("https://docs.google.com/gview?embedded=true&url=" + URLEncoder.encode(prescription, "ISO-8859-1"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }

            binding.webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    return false;
                }
            });

            binding.webView.setWebChromeClient(new WebChromeClient() {
                public void onProgressChanged(WebView view, int progress) {
                    if (progress == 100) {
                        binding.progressBar2.setVisibility(View.GONE);
                    }
                }
            });

            binding.executePendingBindings();
        }
    }

    public interface AttachmentListener {
        void onDeleteClick(String url);
    }
}
