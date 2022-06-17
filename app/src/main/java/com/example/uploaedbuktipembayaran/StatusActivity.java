package com.example.uploaedbuktipembayaran;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.uploaedbuktipembayaran.Util.ServerAPI;
import com.google.android.material.snackbar.Snackbar;

import com.itextpdf.text.pdf.draw.LineSeparator;
import com.kosalgeek.android.photoutil.GalleryPhoto;
import com.kosalgeek.android.photoutil.ImageBase64;
import com.kosalgeek.android.photoutil.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StatusActivity extends AppCompatActivity {
    int bayar, kembali;
    TextView status, tanggal, nota, username, total;
    ImageView imgStatus, gambar;
    Button btnsimpan, btncetak, btngallery;
    ProgressDialog pd;
    DecimalFormat decimalFormat;
    GalleryPhoto mGalery;
    String encode_image = null;
    private final int TAG_GALLERY = 2222;
    String selected_photo = null;
    public static final String TAG_USERNAME = "username";
    String date;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        status = (TextView) findViewById(R.id.text_status);
        tanggal = (TextView) findViewById(R.id.tanggal);
        nota = (TextView) findViewById(R.id.no_nota);
        username = (TextView) findViewById(R.id.user);
        total = (TextView) findViewById(R.id.total_biaya);
        imgStatus = (ImageView) findViewById(R.id.img_status);
        gambar = (ImageView) findViewById(R.id.inp_gambar);
        btngallery = (Button) findViewById(R.id.btn_gallery);
        btncetak = (Button) findViewById(R.id.btn_cetak);
        btnsimpan = (Button) findViewById(R.id.btn_simpan);
        date = new SimpleDateFormat("yyyy-MM-dd",  Locale.getDefault()).format(new Date());
        decimalFormat = new DecimalFormat("#,##0.00");
        pd = new ProgressDialog(StatusActivity.this);
        mGalery = new GalleryPhoto(getApplicationContext());

        loadData();
        btngallery.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View view) {
                                                      startActivityForResult(mGalery.openGalleryIntent(),
                                                              TAG_GALLERY);
                                                  }
                                              });
        btncetak.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
//                createPDF();
            }
        });
        btnsimpan.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                if (selected_photo != null) {
                    simpanData();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Upload bukti pembayaran terlebih dahulu", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void loadData() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.PUT,ServerAPI.URL_DASHBOARD_JUAL,null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                tanggal.setText(jsonObject.getString("tgl_jual"));
                                nota.setText(jsonObject.getString("no_nota"));
                                bayar = jsonObject.getInt("pembayaran");
                                username.setText(jsonObject.getString("username"));
                                kembali = jsonObject.getInt("kembalian");
                                total.setText("Rp. " +  decimalFormat.format(jsonObject.getInt("total_biaya")));
                                if (jsonObject.getInt("status") == 1) {
                                    status.setText("Sudah  Dibayar");
                                    status.setTextColor(Color.GREEN);
                                    imgStatus.setImageResource(R.drawable.berhasil);
                                    btngallery.setVisibility(View.GONE);
                                    btncetak.setVisibility(View.GONE);
                                    btnsimpan.setVisibility(View.GONE);
                                    findViewById(R.id.rekening).setVisibility(View.GONE);
                                } else if (jsonObject.getInt("status") ==  0) {
                                    status.setText("Belum  Dibayar");
                                    status.setTextColor(Color.RED);
                                    imgStatus.setImageResource(R.drawable.gagal);
                                    btncetak.setVisibility(View.GONE);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override

                    public void onErrorResponse(VolleyError error) {
                        pd.cancel();

                        Log.d("volley", "error : " +
                                error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("username",
                        username.getText().toString());
                return map;
            }
        };
        AppController.getInstance().addToRequestQueue(jsonArrayRequest);
    }

    private void simpanData() {
        pd.setMessage("Mengirim Data");
        pd.setCancelable(false);
        pd.show();
        try {
            Bitmap bitmap = ImageLoader.init().from(selected_photo).requestSize(1024,
                            1024).getBitmap();
            encode_image = ImageBase64.encode(bitmap);
            Log.d("ENCODER", encode_image);
            StringRequest sendData = new
                    StringRequest(Request.Method.POST, ServerAPI.URL_JUAL2,
                    new Response.Listener<String>() {
                        @Override

                        public void onResponse(String response)

                        {
                            pd.cancel();
                            try {
                                JSONObject res = new
                                        JSONObject(response);
                                Toast.makeText(StatusActivity.this, "pesan : " + res.getString("message"), Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Intent intent = new  Intent(getApplicationContext(), StatusActivity.class);
                            intent.putExtra(TAG_USERNAME,  username.getText().toString());
                            startActivity(intent);
                            finish();
                        }
                    },
                            new Response.ErrorListener() {
                                @Override

                                public void onErrorResponse(VolleyError error) {
                                    pd.cancel();
                                    Toast.makeText(StatusActivity.this, "pesan : Gagal Kirim Data", Toast.LENGTH_SHORT).show();
                                }
                            }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> map = new HashMap<>();
                            map.put("no_nota",
                                    nota.getText().toString());
                            map.put("gambar", encode_image);
                            return map;
                        }
                    };

            AppController.getInstance().addToRequestQueue(sendData);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode ==  RESULT_OK && requestCode == TAG_GALLERY && data != null && data.getData() != null){

            Uri uri_path = data.getData();

            try {
                Bitmap bitmap;

                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri_path);
                gambar.setImageBitmap(bitmap);

                Snackbar.make(findViewById(android.R.id.content), "Success Loader Image", Snackbar.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();

                Snackbar.make(findViewById(android.R.id.content), "Something Wrong", Snackbar.LENGTH_SHORT).show();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
//    public void createPDF() {
//        SimpleDateFormat s = new  SimpleDateFormat("ddMMyyyyhhmmss");
//        String format = s.format(new Date());
//        Document doc = new Document();
//        String outPath = FileUtils(getApplicationContext()) + "/" + format + ".pdf";
//        try {
//            PdfWriter.getInstance(doc, new FileOutputStream(outPath));
//            doc.open();
//// Document Settings
//            doc.setPageSize(PageSize.A4);
//            doc.addCreationDate();
//            doc.addAuthor("Ivan");
//            doc.addCreator("Ivan");
///**
// * How to USE FONT....
// */
//            BaseFont urName = BaseFont.createFont("assets/fonts/brandon_medium.otf", "UTF-8", BaseFont.EMBEDDED);
///***
// * Variables for further use....
// */
//            BaseColor mColorAccent = new BaseColor(0, 153, 204, 255);
//            float mHeadingFontSize = 20.0f;
//            float mValueFontSize = 26.0f;
//            Font mTitleFont = new Font(urName, 36.0f, Font.NORMAL, BaseColor.BLACK);
//            Font mHeadingFont = new Font(urName, mHeadingFontSize, Font.NORMAL, mColorAccent);
//            Font mValueFont = new Font(urName, mValueFontSize, Font.NORMAL, BaseColor.BLACK);
//// LINE SEPARATOR
//            LineSeparator lineSeparator = new LineSeparator();
//            lineSeparator.setLineColor(new BaseColor(0, 0, 0,
//                    68));
//
//            // Order Details...
//// Title
//            Chunk mTitleChunk = new Chunk("Invoice", mTitleFont);
//            Paragraph mTitleParagraph = new Paragraph(mTitleChunk);
//            mTitleParagraph.setAlignment(Element.ALIGN_CENTER);
//            doc.add(mTitleParagraph);
//// Adding Line Breakable Space....
//            doc.add(new Paragraph(""));
//// Adding Horizontal Line...
//            doc.add(new Chunk(lineSeparator));
//// Adding Line Breakable Space....
//            doc.add(new Paragraph(""));
//// Tanggal
//            Chunk mDateChunk = new Chunk("Order Date:", mHeadingFont);
//            Paragraph mOrderDateParagraph = new Paragraph(mDateChunk);
//            doc.add(mOrderDateParagraph);
//            Chunk mDateValueChunk = new Chunk(date,  mValueFont);
//            Paragraph mDateValueParagraph = new Paragraph(mDateValueChunk);
//            doc.add(mDateValueParagraph);
//            doc.add(new Paragraph(""));
//            doc.add(new Chunk(lineSeparator));
//            doc.add(new Paragraph(""));
//// Akun
//            Chunk mAcNameChunk = new Chunk("Account Name:",  mHeadingFont);
//            Paragraph mAcNameParagraph = new Paragraph(mAcNameChunk);
//            doc.add(mAcNameParagraph);
//            Chunk mAcNameValueChunk = new Chunk(username.getText().toString(), mValueFont);
//            Paragraph mAcNameValueParagraph = new Paragraph(mAcNameValueChunk);
//            doc.add(mAcNameValueParagraph);
////adds paragraph and line seperator
//            doc.add(new Paragraph(""));
//            doc.add(new Chunk(lineSeparator));
//            doc.add(new Paragraph(""));
//// Total
//            Chunk mAmountChunk = new Chunk("Total:", mHeadingFont);
//            Paragraph mAmountParagraph = new Paragraph(mAmountChunk);
//            doc.add(mAmountParagraph);
//            Chunk mAmountValueChunk = new Chunk(total.getText().toString(), mValueFont);
//            Paragraph mAmountValueParagraph = new Paragraph(mAmountValueChunk);
//            doc.add(mAmountValueParagraph);
////adds paragraph and line seperator
//            doc.add(new Paragraph(""));
//            doc.add(new Chunk(lineSeparator));
//            doc.add(new Paragraph(""));
//// Pembayaran
//            Chunk mCashChunk = new Chunk("Cash:",  mHeadingFont);
//            Paragraph mCashParagraph = new Paragraph(mCashChunk);
//            doc.add(mCashParagraph);
//            Chunk mCashValueChunk = new Chunk("Rp. " + decimalFormat.format(bayar), mValueFont);
//            Paragraph mCashValueParagraph = new
//                    Paragraph(mCashValueChunk);
//            doc.add(mCashValueParagraph);
////adds paragraph and line seperator
//            doc.add(new Paragraph(""));
//            doc.add(new Chunk(lineSeparator));
//            doc.add(new Paragraph(""));
//// Kembalian
//            Chunk mChangeChunk = new Chunk("Change:", mHeadingFont);
//            Paragraph mChangeParagraph = new Paragraph(mChangeChunk);
//            doc.add(mChangeParagraph);
//            Chunk mChangeValueChunk = new Chunk("Rp. " + decimalFormat.format(kembali), mValueFont);
//            Paragraph mChangeValueParagraph = new Paragraph(mChangeValueChunk);
//            doc.add(mChangeValueParagraph);
////adds paragraph and line seperator
//            doc.add(new Paragraph(""));
//            doc.add(new Chunk(lineSeparator));
//            doc.add(new Paragraph(""));
//            doc.close();
//            FileUtils.openFile(getApplicationContext(), new File(outPath));
//        } catch (DocumentException e) {
//            e.printStackTrace();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (DocumentException e) {
//            e.printStackTrace();
//        }
//    }
}