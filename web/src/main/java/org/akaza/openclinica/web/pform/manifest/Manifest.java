package org.akaza.openclinica.web.pform.manifest;

import javax.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="manifest")
@XmlAccessorType(XmlAccessType.FIELD)
public class Manifest {
    private ArrayList<MediaFile> mediaFile = null;

    public Manifest() {
        mediaFile = new ArrayList<MediaFile>();
    }

    public void add(MediaFile mediaFile) {
        this.mediaFile.add(mediaFile);
    }

    public ArrayList<MediaFile> getMediaFile() {
        return mediaFile;
    }

    public void setMediaFiles(ArrayList<MediaFile> mediaFile) {
        this.mediaFile = mediaFile;
    }
}
