package com.ahmadinfotech.salaryreport.export;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.ahmadinfotech.salaryreport.R;
import com.ahmadinfotech.salaryreport.dao.PdfDao;
import com.ahmadinfotech.salaryreport.dao.Transaction;
import com.ahmadinfotech.salaryreport.db.FetchData;
import com.ahmadinfotech.salaryreport.fragment.DatewiseReport;
import com.ahmadinfotech.salaryreport.utils.AppConstants;
import com.ahmadinfotech.salaryreport.utils.AppUtils;
import com.ahmadinfotech.salaryreport.utils.SessionManager;
import com.itextpdf.text.Anchor;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.html.HtmlUtilities;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.File;
import java.io.FileOutputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static android.R.attr.documentLaunchMode;
import static android.R.attr.format;
import static com.ahmadinfotech.salaryreport.R.color.month;
import static com.ahmadinfotech.salaryreport.R.id.ca;
import static com.ahmadinfotech.salaryreport.R.id.txtFromDate;
import static com.ahmadinfotech.salaryreport.R.id.txtToDate;
import static java.security.AccessController.getContext;

@SuppressLint({"Instantiatable"})
public class ReportGenerator {
  private static Font catFont = new Font(FontFamily.TIMES_ROMAN, 18.0f, 1);
  private static Format dateFormatSystem;
  private static List<String> mColumns;
  private static Context mContext;
  private static PdfDao mHeader;
  private static int mIndex;
  private static List<PdfDao> mValues;
  private static Font smallBold = new Font(FontFamily.TIMES_ROMAN, HtmlUtilities.DEFAULT_FONT_SIZE, 1);
  private static Font subFont = new Font(FontFamily.TIMES_ROMAN, 16.0f, 1);
    static Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.BLACK);
  static double openingBal,closingBal,totalCR,totalDR;
  static String fromDate, toDate;

  private class Task extends AsyncTask<String, Void, String> {
    private String filePath;

    private Task() {
    }

    protected String doInBackground(String... urls) {
      try {
        String PATH = Environment.getExternalStorageDirectory() + AppConstants.FOLDER + "/pdf";
        File directory = new File(PATH);
        if (!directory.exists()) {
          directory.mkdirs();
        }
        this.filePath = ReportGenerator.this.getPDFfileName(PATH);
        Log.e("PDF Task", "path: 16842794");
        Document document = new Document(PageSize.A4,20,20,20,20);
        PdfWriter.getInstance(document, new FileOutputStream(this.filePath));
        document.open();
        ReportGenerator.addMetaData(document);
        ReportGenerator.addContent(document);
        document.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

    protected void onPostExecute(String result) {
      ReportGenerator.this.openPDF(this.filePath);
    }
  }

  @SuppressLint({"Instantiatable"})
  public ReportGenerator(Context applicationContext, String fromDate, String toDate) {
    mContext = applicationContext;
    this.fromDate = fromDate;
    this.toDate = toDate;
    dateFormatSystem = AppUtils.getDateFormat();

    mColumns = new ArrayList();
    mColumns.add("Date");
    mColumns.add("Credit");
    mColumns.add("Debit");
    mColumns.add("Balance Amount");
    mColumns.add("Naration");
  }

  public void pdf(Activity context) {
    if (isStoragePermissionGranted(context)) {
      checkSDCard();
    } else {
      Toast.makeText(context, R.string.permission_storage, Toast.LENGTH_LONG).show();
    }
  }

  public boolean isStoragePermissionGranted(Activity context) {
    if (ContextCompat.checkSelfPermission(context, "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
      return true;
    }
    ActivityCompat.requestPermissions(context, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
    return false;
  }

  private void checkSDCard() {
    String state = Environment.getExternalStorageState();
    Log.v("db export", "storage state is " + state);
    if (!"mounted".equals(state)) {
      Toast.makeText(mContext.getApplicationContext(), "Sorry! SD card not found.", Toast.LENGTH_LONG).show();
    } else if ("mounted_ro".equals(state)) {
      Toast.makeText(mContext.getApplicationContext(), "Please! Insert sd card ", Toast.LENGTH_LONG).show();
    } else {
      new Task().execute(new String[0]);
      Toast.makeText(mContext.getApplicationContext(), "Data exported in pdf file and saved on /SimpleAccounting/pdf folder Succesfully ", Toast.LENGTH_SHORT).show();
    }
  }

  public String getPDFfileName(String folder) {
    String time = AppUtils.getUniqueFileName();
    try {
      String filePath = AppUtils.getValidFileName(mHeader.getFirst());
      Log.e("PDF Task", "filePath: " + filePath);
      return folder + File.separator + filePath + time + ".pdf";
    } catch (Exception e) {
      return folder + File.separator + time + ".pdf";
    }
  }

  private void openPDF(String filePath) {
    File file = new File(filePath);
    Intent target = new Intent("android.intent.action.VIEW");
    target.setDataAndType(Uri.fromFile(file), "application/pdf");
    Intent intent = Intent.createChooser(target, "Open File");
    try {
      //intent.addFlags(DriveFile.MODE_READ_ONLY);
      mContext.startActivity(intent);
    } catch (ActivityNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static void addMetaData(Document document) {
    document.addTitle("Simple Accounting");
    document.addSubject("Ledger");
    document.addKeywords("Java, PDF, iText");
    document.addAuthor("ADSL Infotech");
    document.addCreator("ADSL Infotech");
  }

  private static void addContent(Document document) throws DocumentException {
    try{
        generate(document);
    }
    catch (Exception e){
        e.printStackTrace();
    }
  }

  private static void createTable(Document document) throws BadElementException {
    try{
        totalCR = 0;
      totalDR = 0;
      int i;
      int len = mColumns.size();
      PdfPTable table = new PdfPTable(len);
      table.setSpacingBefore(10);
      table.setSpacingAfter(10);
        table.setTotalWidth(PageSize.A4.getWidth()-40f);
        table.setLockedWidth(true);
      for (i = 0; i < len; i++) {
        PdfPCell c1 = new PdfPCell(new Phrase((String) mColumns.get(i), boldFont));
        c1.setHorizontalAlignment(1);
          c1.setUseAscender(true);
          c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c1.setBackgroundColor(new BaseColor(157, 223, 237));
        table.addCell(c1);
      }
      table.setHeaderRows(1);
      int j = 1;
      for (PdfDao dao : mValues) {
          if(dao.isCrDr()){
              totalCR += Double.parseDouble(dao.getSecond());
          }
          else{
              totalDR += Double.parseDouble(dao.getThird());
          }
        for (i = 0; i < len; i++) {
          switch (i) {
            case 0:
              table.addCell("" + dao.getFirst());
              break;
            case 1:
              table.addCell("" + dao.getSecond());
              break;
            case 2:
              table.addCell("" + dao.getThird());
              break;
            case 3:
              table.addCell("" + dao.getFour());
              break;
            case 4:
              table.addCell("" + dao.getFive());
              break;
            case 5:
              table.addCell("" + dao.getSix());
              break;
            default:
              break;
          }
        }
        j++;
      }
      Paragraph p = new Paragraph();
        p.setIndentationLeft(20);
        p.setIndentationRight(20);
        p.add(table);
      document.add(p);

        Paragraph pp = new Paragraph("Total Credit : "+totalCR+"\nTotal Debit : "+totalDR+"\nClosing Balance : "+closingBal, smallBold);
        pp.setLeading(15);
        document.add(pp);
        document.add(Chunk.NEWLINE);
        LineSeparator ls = new LineSeparator();
        document.add(new Chunk(ls));
    }
    catch (Exception e){
      e.printStackTrace();
    }
  }

  private static void addEmptyLine(Paragraph paragraph, int number) {
    for (int i = 0; i < number; i++) {
      paragraph.add(new Paragraph(" "));
    }
  }

  public static void generate(Document document){
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    try{
      Date date = format.parse(fromDate);
      Date date2 = format.parse(toDate);

      Paragraph preface = new Paragraph("Salary Report from "+new SimpleDateFormat("MMM-yyyy").format(date)+" to "+
              new SimpleDateFormat("MMM-yyyy").format(date2));
      preface.setAlignment(Element.ALIGN_CENTER);
      document.add(preface);

      int months = monthsBetweenDates(date, date2);

      Log.d(DatewiseReport.class.getSimpleName(), "Total months between two dates "+months);

      openingBal = new FetchData().getOpeningBalance(fromDate);
      Log.d("DatewiseReport :: ","Opening Balance :: "+openingBal);

      for(int i = 0; i < months; i++){
        addMonth(document, i);
      }
    }
    catch(Exception e){
      Toast.makeText(mContext,e.getMessage(), Toast.LENGTH_LONG);
        e.printStackTrace();
    }

  }

  public static void addMonth(Document document, int month){
    try{
      //closingBal = openingBal;
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat format2 = new SimpleDateFormat("MMM-yyyy");

      Calendar calendar = Calendar.getInstance();
      calendar.setTime(format.parse(fromDate));
      calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+month);
      String updatedFromDate = format.format(calendar.getTime());

        Calendar c = Calendar.getInstance();
      c.setTime(calendar.getTime());
      c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
      String updatedToDate = format.format(c.getTime());

      ArrayList<Transaction> transactions = new FetchData().getTransactions(mContext, updatedFromDate, updatedToDate, true);

      if(transactions.size() == 0){
        return;
      }

      Paragraph p = new Paragraph("\nMonth : "+format2.format(calendar.getTime())+"\nOpening Balance : "+openingBal, smallBold);
      p.setLeading(15);
      document.add(p);


      List<Transaction> trans = transactions;
      mValues = new ArrayList();
      if (!SessionManager.getListOrder()) {
        Collections.reverse(trans);
      }
      for (Transaction dao : trans) {
        PdfDao pdf = new PdfDao();
        pdf.setFirst(dao.getDate());
        pdf.setSecond("" + dao.getCraditAmount());
        pdf.setThird("" + dao.getDebitAmount());
        pdf.setFour(dao.getBalance());
        pdf.setFive(dao.getNarration());
          pdf.setCrDr(dao.getDr_cr() == 1);
        mValues.add(pdf);

        openingBal += dao.getCraditAmount() + dao.getDebitAmount();
      }

      createTable(document);

      //openingBal = closingBal;
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  public static int monthsBetweenDates(Date startDate, Date endDate){

    Calendar start = Calendar.getInstance();
    start.setTime(startDate);

    Calendar end = Calendar.getInstance();
    end.setTime(endDate);

    int monthsBetween = 0;
    int dateDiff = end.get(Calendar.DAY_OF_MONTH)-start.get(Calendar.DAY_OF_MONTH);

    if(dateDiff<0) {
      int borrrow = end.getActualMaximum(Calendar.DAY_OF_MONTH);
      dateDiff = (end.get(Calendar.DAY_OF_MONTH)+borrrow)-start.get(Calendar.DAY_OF_MONTH);
      monthsBetween--;

      if(dateDiff>0) {
        monthsBetween++;
      }
    }
    else {
      monthsBetween++;
    }
    monthsBetween += end.get(Calendar.MONTH)-start.get(Calendar.MONTH);
    monthsBetween  += (end.get(Calendar.YEAR)-start.get(Calendar.YEAR))*12;
    return monthsBetween;
  }
}
