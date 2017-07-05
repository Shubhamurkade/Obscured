package com.example.android.obscured;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.example.android.obscured.DatabaseUtilities.PicsContract;

import java.io.File;
import java.io.IOException;

/**
 * Created by Shubham on 05-07-2017.
 */

public class MethodsDeclarations {


    public static boolean isHidden(String imagePath)
    {
        int pathLen = imagePath.length();
        while(imagePath.charAt(--pathLen)!='/')
        {

        }

        if(imagePath.charAt(++pathLen) == '.')
            return true;
        return  false;
    }
    public static void DeleteImageFromMediaStoreAndStoreHiddenPathInTable(Context context, String imagePath, boolean isHidden)
    {

        if(!isHidden)
        {
            File imageFile = new File(imagePath);
            int pathLen = imagePath.length();
            System.out.println(imagePath);

            while(imagePath.charAt(--pathLen)!='/')
            {

            }

            String newImagePath = imagePath.substring(0, ++pathLen)+ '.' +imagePath.substring(pathLen);
            File requiredImageName = new File(newImagePath);

            try
            {
                requiredImageName.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            System.out.println(newImagePath);
            if(imageFile.renameTo(requiredImageName))
            {
                //Toast.makeText(this, newImagePath, Toast.LENGTH_LONG).show();
            }

            if(imageFile.delete())
            {
                //Toast.makeText(this, "Deleted file", Toast.LENGTH_LONG).show();
                System.out.println("File deleted");
            }
            else if(imageFile.exists())
            {
                //Toast.makeText(this, "File exists", Toast.LENGTH_LONG).show();
            }

            /* create contentvalues to insert hidden image path (newImagePath);*/
            ContentValues contentValuesToInsert = new ContentValues();
            contentValuesToInsert.put(PicsContract.PicsEntry.PIC_DATA, newImagePath);

            /* common Uri to perform insert or delete */
            Uri uriToInsertDelete = PicsContract.PicsEntry.CONTENT_URI.buildUpon().appendPath("12").build();

            /* delete the existing path of unhidden image */
            context.getContentResolver().delete(uriToInsertDelete, PicsContract.PicsEntry.PIC_DATA + " = ?", new String[]{imagePath});
            /* insert the new hidden path of the image */
            context.getContentResolver().insert(uriToInsertDelete, contentValuesToInsert);

        }

            /* I found out that whenever a row of MediaStore class was delted, the corresponding file would also get deleted
            * In the above case when the unhidden file was added to table during runtime, the following happens:
            *   1.  The file is renamed to hidden file name.
            *   2.  The unhidden file name is deleted from MediaStore. Hence, the unhidden file is also deleted.
            *   3.  When the app is launched again the hidden file name is added to MediaStore class. Hence, the
            *       image is visible in the main activity.
            *
            * In the case when the table already has hidden image path the following would happen without the below else part:
            *   1.  The hidden image would be deleted from the MediaStore class. Hence, the corresponding image would also be delted.
            *   2.  When the app is launched again, the image path would be added from the table to the MediaStore class.
            *       Since, the image isn't actually present in the SD card, a white image would instead be displayed.
            *
            * After the fix the following would happen:
            *   1.  The hidden image path would be deleted from the MediaStore class. Hence, the corresponding image would also be deleted.
            *   2.  A new file with an incremental number attached to its actual name would be created.
            *   3.  This backup image would be the same as the original image.
            *   4.  When the app is launched again, the new backup image path would then be copied from the table into the MediaStore class.
            */
        else
        {
            File imageFile = new File(imagePath);
            int pathLen = imagePath.length();
            System.out.println(imagePath);
            int numAtLast;

                /* Create a hidden file name as follows:
                *  If the hidden file is created for the first time then append the file name with _0
                *  Else append the hidden file name with _(previous number + 1)
                *  If the previous number was 9 the append with _0.
                */

                /* Get the sub-string until '.jpg' is is reached */

            while(imagePath.charAt(--pathLen)!='.')
            {

            }

            int originalPathLen = pathLen;

            String subImagePath = imagePath.substring(0, pathLen);

            String newImagePath;

                /* Check if the 2nd char from last is '_' */
            if(subImagePath.charAt(pathLen - 2) == '_')
            {
                numAtLast = Integer.parseInt(subImagePath.charAt(pathLen - 1)+"");
                if(numAtLast == 9)
                    numAtLast = 0;
                else numAtLast++;

                subImagePath = subImagePath.substring(0, pathLen - 1);
                newImagePath = subImagePath + "" + numAtLast;
            }
            else
            {
                numAtLast = 0;
                newImagePath = subImagePath + "_" + numAtLast;
            }

                /* subImagePath_{0-9}.jpg */
            newImagePath = newImagePath + imagePath.substring(originalPathLen);
            File requiredImageName = new File(newImagePath);

            try
            {
                requiredImageName.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            if(imageFile.renameTo(requiredImageName))
            {
                Toast.makeText(context, newImagePath, Toast.LENGTH_LONG).show();
            }

            if(imageFile.delete())
            {
                Toast.makeText(context, "Deleted file", Toast.LENGTH_LONG).show();
            }
            else if(imageFile.exists())
            {
                Toast.makeText(context, "File exists", Toast.LENGTH_LONG).show();
            }

            /* create contentvalues to insert hidden image path (newImagePath);*/
            ContentValues contentValuesToInsert = new ContentValues();
            contentValuesToInsert.put(PicsContract.PicsEntry.PIC_DATA, newImagePath);

            /* common Uri to perform insert or delete */
            Uri uriToInsertDelete = PicsContract.PicsEntry.CONTENT_URI.buildUpon().appendPath("12").build();

            /* delete the existing path of unhidden image */
            context.getContentResolver().delete(uriToInsertDelete, PicsContract.PicsEntry.PIC_DATA + " = ?", new String[]{imagePath});
            /* insert the new hidden path of the image */
            context.getContentResolver().insert(uriToInsertDelete, contentValuesToInsert);
        }

        /* if it's already a hidden image path then only delete from MediaStore. It is required
        * to be deleted from MediaStore because when the app is launched all the images from the
        * table (which in this case while launching the app will be hidden images path) will be
        * added to the MediaStore class*/

        context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA+" = ?", new String[]{imagePath});

    }
}
