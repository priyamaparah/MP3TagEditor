package helper;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;


import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.IMusicMetadata;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import com.example.mymp3tageditor.Mp3Info;

import de.vdheide.mp3.FrameDamagedException;
import de.vdheide.mp3.ID3Exception;
import de.vdheide.mp3.ID3v2DecompressionException;
import de.vdheide.mp3.ID3v2Exception;
import de.vdheide.mp3.ID3v2IllegalVersionException;
import de.vdheide.mp3.ID3v2WrongCRCException;
import de.vdheide.mp3.MP3File;
import de.vdheide.mp3.NoMP3FrameException;
import de.vdheide.mp3.TagContent;
import de.vdheide.mp3.TagFormatException;


public class Mp3Utility {
	
	public static final String TAG = "Mp3Utility";

	public static Mp3Info getMP3FileInfo(String mp3FilePath) throws ID3v2WrongCRCException, ID3v2DecompressionException, ID3v2IllegalVersionException, IOException, NoMP3FrameException {
		MP3File mp3File = new MP3File(mp3FilePath);
        Mp3Info info = new Mp3Info();
		TagContent tagContent = null;
		try {
			tagContent = mp3File.getAlbum();
                info.albumName = tagContent.getTextContent();
            tagContent = mp3File.getTitle();
                info.songTitle = tagContent.getTextContent();
            tagContent = mp3File.getTime();
                info.songDuration = tagContent.getTextContent();
            tagContent = mp3File.getArtist();
                info.artistName = tagContent.getTextContent();
		} catch (FrameDamagedException e) {
			Log.e(TAG, e.toString());
			return null;
		}
		Log.i(TAG, "Tag-Album :" +tagContent.getTextContent());
		
		return new Mp3Info(tagContent.getTextContent(), null);	
	}
	
	public static String setMP3FileInfo(String mp3FilePath, Mp3Info mp3Info) throws ID3Exception, ID3v2Exception, IOException, NoMP3FrameException {
		MP3File mp3File = new MP3File(mp3FilePath);
		TagContent newTag = new TagContent();
		Log.i(TAG,"Album Name to be set :" + mp3Info.albumName);
		newTag.setContent(mp3Info.albumName);
		try {
			mp3File.setAlbum(newTag);
		} catch (TagFormatException e) {
			Log.e(TAG, e.toString());
			return null;
		}
		
		mp3File.update();
		return mp3Info.albumName;
	}

    public static Mp3Info getMP3FileInfo2(String mp3FilePath) {
        File src = new File(mp3FilePath);
        MusicMetadataSet src_set = null;
        try {
            src_set = new MyID3().read(src);
        } catch (IOException e1) {
            e1.printStackTrace();
        } // read metadata

        if (src_set == null) // perhaps no metadata
        {
            Log.i(TAG, "sournce file is NULL");
        }
        else
        {
            String title=null,album = null;
            try{
                IMusicMetadata metadata = src_set.getSimplified();
                 title = metadata.getSongTitle();
                 album = metadata.getAlbum();
                String song_title = metadata.getSongTitle();
                Number track_number = metadata.getTrackNumber();
                Log.i(TAG, "album - " +album);
                Log.i(TAG, "Song title - " + title);
                Log.i(TAG, "Duration - " + convertTimeSecToMin(metadata.getDurationSeconds()));
            }catch (Exception e) {
                e.printStackTrace();
            }
            return new Mp3Info(album, title);
        }
        return null;
    }

    public static String setMP3FileInfo2(String mp3FilePath, Mp3Info mp3Info)  {
        File src = new File(mp3FilePath);
        MusicMetadata meta = new MusicMetadata("name");
        Log.i(TAG,"Album Name to be set :" + mp3Info.albumName);
        if(!TextUtils.isEmpty(mp3Info.albumName)) meta.setAlbum(mp3Info.albumName);
        if(!TextUtils.isEmpty(mp3Info.songTitle))
        		meta.setSongTitle(mp3Info.songTitle);
//        meta.setArtist("CS");
        try {
            MusicMetadataSet src_set = new MyID3().read(src);
            new MyID3().update(src, src_set, meta);
            return meta.getAlbum();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ID3WriteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }  // write updated metadata
        return null;
    }
    
   

    public static void scanSDCardFile(Context ctx, String[] filePaths) {
        MediaScannerConnection.scanFile(ctx, filePaths, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }

    static String[] sizeUnits = new String[] { "B", "KB", "MB"};
    /**
     *
     * @param  filesize
     * @return B/KB/MB/GB format
     */
    public static String formattedFileSize(long filesize) {
        if(filesize <= 0) return "0";
        int digGroup = (int) (Math.log10(filesize)/Math.log10(1024));

        return new DecimalFormat("#,##0.00")
                .format(filesize/Math.pow(1024, digGroup)) + " " + sizeUnits[digGroup];
    }

    private static final String FTYPE = ".mp3";
    public static FilenameFilter getFileNameFilter(final boolean withDirectory) {
        return new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                File sel = new File(dir, filename);
                return (!sel.isHidden()) && (filename.contains(FTYPE) || filename.contains(".MP3")
                        || (withDirectory? (sel.isDirectory() ? (sel.list()==null? false : true) : false) : false));
            }
        };
    }

    public static String convertTimeSecToMin(String seconds) {
        int s = -1;
        try {
            s = Integer.parseInt(seconds);
        } catch (NumberFormatException ne) {
            Log.e(TAG, "convertTimeSecToMin() " + ne);
            return null;
        }
        int min = s/60;
        int sec = s % 60;
        return String.format("%d:%02d", min, sec);
    }

    public static String convertTimeMilliToMin(int ms) {
        return String.format("%d m, %d s",
                TimeUnit.MILLISECONDS.toMinutes(ms),
                TimeUnit.MILLISECONDS.toSeconds(ms) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms))
        );
    }
}
