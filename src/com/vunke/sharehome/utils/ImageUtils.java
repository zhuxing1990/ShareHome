package com.vunke.sharehome.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

/**
 * 图片工具类
 * @author zhuxi
 *
 */
public class ImageUtils {

	public static final int GET_IMAGE_BY_CAMERA = 5001;
	public static final int GET_IMAGE_FROM_PHONE = 5002;
	public static final int CROP_IMAGE = 5003;
	public static Uri imageUriFromCamera;
	public static Uri cropImageUri;

	public static void openCameraImage(final Activity activity) {
		ImageUtils.imageUriFromCamera = ImageUtils.createImagePathUri(activity);

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// MediaStore.EXTRA_OUTPUT参数不设置时,系统会自动生成一个uri,但是只会返回一个缩略图
		// 返回图片在onActivityResult中通过以下代码获取
		// Bitmap bitmap = (Bitmap) data.getExtras().get("data");
		intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageUtils.imageUriFromCamera);
		activity.startActivityForResult(intent, ImageUtils.GET_IMAGE_BY_CAMERA);
	}

	public static void openLocalImage(final Activity activity) {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		activity.startActivityForResult(intent, ImageUtils.GET_IMAGE_FROM_PHONE);
	}

	public static void cropImage(Activity activity, Uri srcUri) {
		ImageUtils.cropImageUri = ImageUtils.createImagePathUri(activity);

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(srcUri, "image/*");
		intent.putExtra("crop", "true");

		// //////////////////////////////////////////////////////////////
		// 1.宽高和比例都不设置时,裁剪框可以自行调整(比例和大小都可以随意调整)
		// //////////////////////////////////////////////////////////////
		// 2.只设置裁剪框宽高比(aspect)后,裁剪框比例固定不可调整,只能调整大小
		// //////////////////////////////////////////////////////////////
		// 3.裁剪后生成图片宽高(output)的设置和裁剪框无关,只决定最终生成图片大小
		// //////////////////////////////////////////////////////////////
		// 4.裁剪框宽高比例(aspect)可以和裁剪后生成图片比例(output)不同,此时,
		// 会以裁剪框的宽为准,按照裁剪宽高比例生成一个图片,该图和框选部分可能不同,
		// 不同的情况可能是截取框选的一部分,也可能超出框选部分,向下延伸补足
		// //////////////////////////////////////////////////////////////

		// aspectX aspectY 是裁剪框宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪后生成图片的宽高
		// intent.putExtra("outputX", 300);
		// intent.putExtra("outputY", 100);

		// return-data为true时,会直接返回bitmap数据,但是大图裁剪时会出现问题,推荐下面为false时的方式
		// return-data为false时,不会返回bitmap,但需要指定一个MediaStore.EXTRA_OUTPUT保存图片uri
		intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageUtils.cropImageUri);
		intent.putExtra("return-data", false);

		activity.startActivityForResult(intent, CROP_IMAGE);
	}

	/**
	 * 创建一条图片地址uri,用于保存拍照后的照片
	 * 
	 * @param context
	 * @return 图片的uri
	 */
	public static Uri createImagePathUri(Context context) {
		Uri imageFilePath = null;
		String status = Environment.getExternalStorageState();
		SimpleDateFormat timeFormatter = new SimpleDateFormat(
				"yyyyMMdd_HHmmss", Locale.CHINA);
		long time = System.currentTimeMillis();
		String imageName = timeFormatter.format(new Date(time));
		// ContentValues是我们希望这条记录被创建时包含的数据信息
		ContentValues values = new ContentValues(3);
		values.put(MediaStore.Images.Media.DISPLAY_NAME, imageName);
		values.put(MediaStore.Images.Media.DATE_TAKEN, time);
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
		if (status.equals(Environment.MEDIA_MOUNTED)) {// 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
			imageFilePath = context.getContentResolver().insert(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		} else {
			imageFilePath = context.getContentResolver().insert(
					MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
		}
		Log.e("tag", "生成的照片输出路径：" + imageFilePath.toString());
		// Log.e("tag", getPath(context, imageFilePath));
		return imageFilePath;
	}

	public static final String TAG = ImageUtils.class.getSimpleName();

	/**
	 * get scaled bitmap 得到缩放位图 
	 * 
	 * @param filePath 文件路径
	 *            local file path 本地文件路径
	 * @param maxWidth 
	 *            scaled bitmap width you desired, if maxWidth < maxHeight, then
	 *            scaled bitmap width is maxWidth while bitmap height is
	 *            maxWidth * ratio
	 * @param maxHeight
	 *            scaled bitmap height you desired, if maxHeight < maxWidth,
	 *            then scaled bitmap height is maxHeight while bitmap width is
	 *            maxHeight / ratio.
	 * @return scaled bitmap
	 */
	public static Bitmap getScaledBitmap(String filePath, int maxWidth,
			int maxHeight) {
		BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
		Bitmap bitmap;
		// If we have to resize this image, first get the natural bounds.
		decodeOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, decodeOptions);
		int actualWidth = decodeOptions.outWidth;
		int actualHeight = decodeOptions.outHeight;
		Log.d(TAG, "Actual width: " + actualWidth + ", actual height: "
				+ actualHeight);
		// Then compute the dimensions we would ideally like to decode to.
		int desiredWidth = getResizedDimension(maxWidth, maxHeight,
				actualWidth, actualHeight);
		int desiredHeight = getResizedDimension(maxHeight, maxWidth,
				actualHeight, actualWidth);
		Log.d(TAG, "Desired width: " + desiredWidth + ", desired height: "
				+ desiredHeight);

		// Decode to the nearest power of two scaling factor.
		decodeOptions.inJustDecodeBounds = false;
		// TODO(ficus): Do we need this or is it okay since API 8 doesn't
		// support it?
		// decodeOptions.inPreferQualityOverSpeed = PREFER_QUALITY_OVER_SPEED;
		decodeOptions.inSampleSize = findBestSampleSize(actualWidth,
				actualHeight, desiredWidth, desiredHeight);
		Bitmap tempBitmap = BitmapFactory.decodeFile(filePath, decodeOptions);
		// If necessary, scale down to the maximal acceptable size.
		if (tempBitmap != null
				&& (tempBitmap.getWidth() > desiredWidth || tempBitmap
						.getHeight() > desiredHeight)) {
			bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth,
					desiredHeight, true);
			tempBitmap.recycle();
		} else {
			bitmap = tempBitmap;
		}
		return bitmap;
	}

	/**
	 * get scaled bitmap 得到缩放位图 
	 * 
	 * @param imageResId   ImageView控件ID
	 *            image resource id
	 * @param maxWidth
	 *            scaled bitmap width you desired, if maxWidth < maxHeight, then
	 *            scaled bitmap width is maxWidth while bitmap height is
	 *            maxWidth * ratio
	 * @param maxHeight
	 *            scaled bitmap height you desired, if maxHeight < maxWidth,
	 *            then scaled bitmap height is maxHeight while bitmap width is
	 *            maxHeight / ratio.
	 * @return scaled bitmap
	 */
	public static Bitmap getScaledBitmap(Context context, int imageResId,
			int maxWidth, int maxHeight) {
		BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
		Bitmap bitmap = null;
		// If we have to resize this image, first get the natural bounds.
		decodeOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(context.getResources(), imageResId,
				decodeOptions);
		int actualWidth = decodeOptions.outWidth;
		int actualHeight = decodeOptions.outHeight;
		Log.d(TAG, "Actual width: " + actualWidth + ", actual height: "
				+ actualHeight);

		// Then compute the dimensions we would ideally like to decode to.
		int desiredWidth = getResizedDimension(maxWidth, maxHeight,
				actualWidth, actualHeight);
		int desiredHeight = getResizedDimension(maxHeight, maxWidth,
				actualHeight, actualWidth);
		Log.d(TAG, "Desired width: " + desiredWidth + ", desired height: "
				+ desiredHeight);

		// Decode to the nearest power of two scaling factor.
		decodeOptions.inJustDecodeBounds = false;
		// TODO(ficus): Do we need this or is it okay since API 8 doesn't
		// support it?
		// decodeOptions.inPreferQualityOverSpeed = PREFER_QUALITY_OVER_SPEED;
		decodeOptions.inSampleSize = findBestSampleSize(actualWidth,
				actualHeight, desiredWidth, desiredHeight);
		Bitmap tempBitmap = BitmapFactory.decodeResource(
				context.getResources(), imageResId, decodeOptions);
		// If necessary, scale down to the maximal acceptable size.
		if (tempBitmap != null
				&& (tempBitmap.getWidth() > desiredWidth || tempBitmap
						.getHeight() > desiredHeight)) {
			bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth,
					desiredHeight, true);
			tempBitmap.recycle();
		} else {
			bitmap = tempBitmap;
		}
		return bitmap;
	}

	/**
	 * 寻找最佳样本大小 
	 * Returns the largest power-of-two divisor for use in downscaling a bitmap
	 * that will not result in the scaling past the desired dimensions.
	 * 
	 * @param actualWidth
	 *            Actual width of the bitmap
	 * @param actualHeight
	 *            Actual height of the bitmap
	 * @param desiredWidth
	 *            Desired width of the bitmap
	 * @param desiredHeight
	 *            Desired height of the bitmap
	 */
	// Visible for testing.
	private static int findBestSampleSize(int actualWidth, int actualHeight,
			int desiredWidth, int desiredHeight) {
		double wr = (double) actualWidth / desiredWidth;
		double hr = (double) actualHeight / desiredHeight;
		double ratio = Math.min(wr, hr);
		float n = 1.0f;
		while ((n * 2) <= ratio) {
			n *= 2;
		}

		return (int) n;
	}

	/**
	 * Scales one side of a rectangle to fit aspect ratio.
	 * 得到大小尺寸 
	 * @param maxPrimary
	 *            Maximum size of the primary dimension (i.e. width for max
	 *            width), or zero to maintain aspect ratio with secondary
	 *            dimension
	 * @param maxSecondary
	 *            Maximum size of the secondary dimension, or zero to maintain
	 *            aspect ratio with primary dimension
	 * @param actualPrimary
	 *            Actual size of the primary dimension
	 * @param actualSecondary
	 *            Actual size of the secondary dimension
	 */
	private static int getResizedDimension(int maxPrimary, int maxSecondary,
			int actualPrimary, int actualSecondary) {
		// If no dominant value at all, just return the actual.
		if (maxPrimary == 0 && maxSecondary == 0) {
			return actualPrimary;
		}

		// If primary is unspecified, scale primary to match secondary's scaling
		// ratio.
		if (maxPrimary == 0) {
			double ratio = (double) maxSecondary / (double) actualSecondary;
			return (int) (actualPrimary * ratio);
		}

		if (maxSecondary == 0) {
			return maxPrimary;
		}

		double ratio = (double) actualSecondary / (double) actualPrimary;
		int resized = maxPrimary;
		if (resized * ratio > maxSecondary) {
			resized = (int) (maxSecondary / ratio);
		}
		return resized;
	}

	/**
	 * get actual image dimension
	 * 获取实际的图像尺寸 
	 * @param imagePath
	 *            local file path
	 * @return
	 */
	public static int[] getActualImageDimension(String imagePath) {
		int[] imageSize = new int[2];
		BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
		// If we have to resize this image, first get the natural bounds.
		decodeOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, decodeOptions);
		int actualWidth = decodeOptions.outWidth;
		int actualHeight = decodeOptions.outHeight;
		imageSize[0] = actualWidth;
		imageSize[1] = actualHeight;
		return imageSize;
	}

	/**
	 * get actual image dimension
	 * 获取实际的图像尺寸 
	 * @param imageResId
	 *            image resource id
	 * @return
	 */
	public static int[] getActualImageDimension(Context context, int imageResId) {
		int[] imageSize = new int[2];
		BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
		// If we have to resize this image, first get the natural bounds.
		decodeOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(context.getResources(), imageResId,
				decodeOptions);
		int actualWidth = decodeOptions.outWidth;
		int actualHeight = decodeOptions.outHeight;
		imageSize[0] = actualWidth;
		imageSize[1] = actualHeight;
		return imageSize;
	}

	/**
	 * 获取所需的图像尺寸 
	 * @param imagePath
	 * @param maxWidth
	 * @param maxHeight
	 * @return
	 */
	private static int[] getDesiredImageDimension(String imagePath,
			int maxWidth, int maxHeight) {
		int[] desiredImageDimension = new int[2];
		int[] actualImageDimension = getActualImageDimension(imagePath);
		Log.d(TAG, "Actual width: " + actualImageDimension[0]
				+ ", actual height: " + actualImageDimension[1]);
		int maxPrimary;
		int maxSecondary;
		if (actualImageDimension[0] >= actualImageDimension[1]) {
			maxPrimary = maxWidth;
			maxSecondary = 0;
			desiredImageDimension[0] = getResizedDimension(maxPrimary,
					maxSecondary, actualImageDimension[0],
					actualImageDimension[1]);
			desiredImageDimension[1] = getResizedDimension(maxSecondary,
					maxPrimary, actualImageDimension[1],
					actualImageDimension[0]);
		} else {
			maxPrimary = maxHeight;
			maxSecondary = 0;
			desiredImageDimension[1] = getResizedDimension(maxPrimary,
					maxSecondary, actualImageDimension[1],
					actualImageDimension[0]);
			desiredImageDimension[0] = getResizedDimension(maxPrimary,
					maxSecondary, actualImageDimension[0],
					actualImageDimension[1]);
		}
		Log.d(TAG, "Desired width: " + desiredImageDimension[0]
				+ ", desired height: " + desiredImageDimension[1]);
		return desiredImageDimension;
	}

	/**
	 * 获取所需的图像尺寸 
	 * @param context
	 * @param imageResId
	 * @param maxWidth
	 * @param maxHeight
	 * @return
	 */
	private static int[] getDesiredImageDimension(Context context,
			int imageResId, int maxWidth, int maxHeight) {
		int[] desiredImageDimension = new int[2];
		int[] actualImageDimension = getActualImageDimension(context,
				imageResId);
		Log.d(TAG, "Actual width: " + actualImageDimension[0]
				+ ", actual height: " + actualImageDimension[1]);
		int maxPrimary;
		int maxSecondary;
		if (actualImageDimension[0] >= actualImageDimension[1]) {
			maxPrimary = maxWidth;
			maxSecondary = 0;
			desiredImageDimension[0] = getResizedDimension(maxPrimary,
					maxSecondary, actualImageDimension[0],
					actualImageDimension[1]);
			desiredImageDimension[1] = getResizedDimension(maxSecondary,
					maxPrimary, actualImageDimension[1],
					actualImageDimension[0]);
		} else {
			maxPrimary = maxHeight;
			maxSecondary = 0;
			desiredImageDimension[1] = getResizedDimension(maxPrimary,
					maxSecondary, actualImageDimension[1],
					actualImageDimension[0]);
			desiredImageDimension[0] = getResizedDimension(maxPrimary,
					maxSecondary, actualImageDimension[0],
					actualImageDimension[1]);
		}
		Log.d(TAG, "Desired width: " + desiredImageDimension[0]
				+ ", desired height: " + desiredImageDimension[1]);
		return desiredImageDimension;
	}

	/**
	 * compress the image file, create a scaled compressed image file, and
	 * overwrite the origin one.
	 * 压缩图像文件，创建缩放压缩图像文件，并覆盖原点。 
	 * @param path
	 *            origin image file path
	 * @param maxWidth
	 * @param maxHeight
	 * @param quality
	 */
	public static void compress(String path, int maxWidth, int maxHeight,
			int quality) {
		FileOutputStream out;
		try {
			Bitmap scaledBitmap = getScaledBitmap(path, maxWidth, maxHeight);
			Bitmap rotatedBitmap = rotateBitmap(getBitmapDegree(path),
					scaledBitmap);
			out = new FileOutputStream(path);
			Bitmap mutableBitmap = rotatedBitmap.copy(Bitmap.Config.ARGB_8888,
					true);

			// write the compressed bitmap at the destination specified by
			// filename.
			mutableBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * compress the image file, create a scaled compressed image file, and
	 * overwrite the origin one.
	 * 压缩图像文件，创建缩放压缩图像文件，并覆盖原点。 
	 * @param path
	 *            origin image file path
	 * @param maxWidth
	 * @param maxHeight
	 * @param quality
	 */
	public static void compress(String originPath, String outputPath,
			int maxWidth, int maxHeight, int quality) {
		FileOutputStream out;
		try {
			Bitmap scaledBitmap = getScaledBitmap(originPath, maxWidth,
					maxHeight);
			Bitmap rotatedBitmap = rotateBitmap(getBitmapDegree(originPath),
					scaledBitmap);
			out = new FileOutputStream(outputPath);
			Bitmap mutableBitmap = rotatedBitmap.copy(Bitmap.Config.ARGB_8888,
					true);

			// write the compressed bitmap at the destination specified by
			// filename.
			mutableBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * add water mark at the left top of image.
	 * 在图像左上方添加加水印。 
	 * @param context
	 * @param srcPath
	 *            local image file path
	 * @param watermarkRes
	 *            watermark resource
	 * @param maxWidth
	 *            scaled bitmap width you desired, if maxWidth < maxHeight, then
	 *            scaled bitmap width is maxWidth while bitmap height is
	 *            maxWidth * ratio
	 * @param maxHeight
	 *            scaled bitmap height you desired, if maxHeight < maxWidth,
	 *            then scaled bitmap height is maxHeight while bitmap width is
	 *            maxHeight / ratio.
	 * @param quality
	 *            compress quality
	 */
	// http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
	public static void watermark(Context context, String srcPath,
			int watermarkRes, int maxWidth, int maxHeight, int quality) {

		FileOutputStream out;
		try {
			Bitmap scaledBitmap = getScaledBitmap(srcPath, maxWidth, maxHeight);
			Bitmap rotatedBitmap = rotateBitmap(getBitmapDegree(srcPath),
					scaledBitmap);
			Bitmap scaledWatermark = getScaledBitmap(context, watermarkRes,
					maxWidth, maxHeight);
			out = new FileOutputStream(srcPath);

			Bitmap mutableBitmap = rotatedBitmap.copy(Bitmap.Config.ARGB_8888,
					true);
			Canvas canvas = new Canvas(mutableBitmap);
			canvas.drawBitmap(scaledWatermark, 0, 0, null);

			// write the compressed bitmap at the destination specified by
			// filename.
			mutableBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			Log.e(TAG, "Add watermark result in OOM");
			e.printStackTrace();
		}
	}

	/**
	 * add watermark at the right bottom of the image
	 * 在图像右下方添加水印 
	 * @param context
	 * @param srcPath
	 *            local image file path
	 * @param watermarkRes
	 *            watermark resource
	 * @param maxWidth
	 *            scaled bitmap width you desired, if maxWidth < maxHeight, then
	 *            scaled bitmap width is maxWidth while bitmap height is
	 *            maxWidth * ratio
	 * @param maxHeight
	 *            scaled bitmap height you desired, if maxHeight < maxWidth,
	 *            then scaled bitmap height is maxHeight while bitmap width is
	 *            maxHeight / ratio.
	 * @param quality
	 *            compress quality
	 */
	public static void watermarkAtRightBottom(Context context, String srcPath,
			int watermarkRes, int maxWidth, int maxHeight, int quality) {

		FileOutputStream out;
		try {
			Bitmap scaledBitmap = getScaledBitmap(srcPath, maxWidth, maxHeight);
			Bitmap rotatedBitmap = rotateBitmap(getBitmapDegree(srcPath),
					scaledBitmap);
			Bitmap scaledWatermark = getScaledBitmap(context, watermarkRes,
					maxWidth, maxHeight);
			out = new FileOutputStream(srcPath);

			int left = rotatedBitmap.getWidth() - scaledWatermark.getWidth();
			int top = rotatedBitmap.getHeight() - scaledWatermark.getHeight();

			Bitmap mutableBitmap = rotatedBitmap.copy(Bitmap.Config.ARGB_8888,
					true);
			Canvas canvas = new Canvas(mutableBitmap);
			canvas.drawBitmap(scaledWatermark, left, top, null);

			// write the compressed bitmap at the destination specified by
			// filename.
			mutableBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			Log.e(TAG, "Add watermark result in OOM");
			e.printStackTrace();
		}
	}

	/**
	 * get bitmap degree, you may get an rotated photo when you take a picture
	 * in some devices.
	 * 
	 * @param path
	 *            local image file path
	 * @return
	 */
	public static int getBitmapDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * rotate bitmap
	 * 图 圆形图
	 * @param angle
	 *            rotate angle
	 * @param bitmap
	 *            origin bitmap
	 * @return rotated bitmap
	 */
	public static Bitmap rotateBitmap(int angle, Bitmap bitmap) {
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
	}

	/**
	 * create a copied image.
	 * 拷贝文件
	 * @param context
	 * @param photoUri
	 *            origin image uri
	 * @param outputPath
	 *            output image uri.
	 * @return true if successfully compressed to the specified stream.
	 * @throws IOException
	 */
	public static boolean copyBitmapFile(Context context, Uri photoUri,
			String outputPath) throws IOException {
		// Load image from path
		InputStream input = context.getContentResolver().openInputStream(
				photoUri);

		// compress it
		Bitmap bitmapOrigin = BitmapFactory.decodeStream(input);
		if (bitmapOrigin == null)
			return false;
		// save to file
		FileOutputStream output = new FileOutputStream(outputPath);
		return bitmapOrigin.compress(Bitmap.CompressFormat.JPEG, 100, output);
	}

	/**
	 * 获得圆形图
	 * @param uri
	 * @return
	 */
	private Bitmap getCircleBitmap(Uri uri) {
		Bitmap src = BitmapFactory.decodeFile(uri.getPath());
		Bitmap output = Bitmap.createBitmap(src.getWidth(), src.getHeight(),
				Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(output);

		Paint paint = new Paint();
		Rect rect = new Rect(0, 0, src.getWidth(), src.getHeight());
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		canvas.drawARGB(0, 0, 0, 0);
		canvas.drawCircle(src.getWidth() / 2, src.getWidth() / 2,
				src.getWidth() / 2, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(src, rect, rect, paint);
		return output;
	}
	
	
}