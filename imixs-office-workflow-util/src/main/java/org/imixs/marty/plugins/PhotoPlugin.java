/*******************************************************************************
 *  Imixs Workflow 
 *  Copyright (C) 2001, 2011 Imixs Software Solutions GmbH,  
 *  http://www.imixs.com
 *  
 *  This program is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation; either version 2 
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  General Public License for more details.
 *  
 *  You can receive a copy of the GNU General Public
 *  License at http://www.gnu.org/licenses/gpl.html
 *  
 *  Project: 
 *  	http://www.imixs.org
 *  	http://java.net/projects/imixs-workflow
 *  
 *  Contributors:  
 *  	Imixs Software Solutions GmbH - initial API and implementation
 *  	Ralph Soika - Software Developer
 *******************************************************************************/

package org.imixs.marty.plugins;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.imixs.workflow.FileData;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.plugins.AbstractPlugin;
import org.imixs.workflow.exceptions.PluginException;

import jakarta.inject.Inject;

/**
 * This plugin supports a comment feature. Comments entered by a user into the
 * field 'txtComment' are stored in the list property 'txtCommentList' which
 * contains a map for each comment. The map stores the username, the timestamp
 * and the comment. The plugin also stores the last comment in the field
 * 'txtLastComment'. The comment can be also controlled by the corresponding
 * workflow event:
 * 
 * <comment ignore="true" /> a new comment will not be added into the comment
 * list
 * 
 * <comment>xxx</comment> adds a fixed comment 'xxx' into the comment list
 * 
 * 
 * @author rsoika
 * @version 1.0
 * 
 */
public class PhotoPlugin extends AbstractPlugin {
    ItemCollection documentContext;

    private static Logger logger = Logger.getLogger(CommentPlugin.class.getName());

    @Inject
    @ConfigProperty(name = "photo.maxwidth", defaultValue = "1024")
    int photoMaxWidth;

    @Inject
    @ConfigProperty(name = "photo.filepattern", defaultValue = "[^.]*\\.(jpg|jpeg|png)$")
    String filePattern;

    /**
     * This method updates resizes images.
     * 
     * @param workflowEvent
     */
    @Override
    public ItemCollection run(ItemCollection workItem, ItemCollection documentActivity) throws PluginException {

        // resize profile image if necessary...
        try {
            List<FileData> fileDataList = workItem.getFileData();
            for (FileData fileData : fileDataList) {
                if (isPhoto(fileData)) {
                    resize(fileData);
                    workItem.addFileData(fileData);
                }
            }
        } catch (Exception e) {
            logger.warning("Unable to resize profile image - " + e.getMessage());
        }

        return workItem;
    }

    /**
     * This method tests all attachments for a pattern and resize the image
     * 
     * @param workitem
     * @return
     * @throws Exception
     */

    public void resize(FileData fileData) throws Exception {

        logger.finest("Image Interceptor started");

        BufferedImage originalImage = getImageFromWorkitem(fileData);

        // test if max with is extended?
        if (originalImage != null && originalImage.getWidth() > photoMaxWidth) {
            logger.info("...resize new photo: " + fileData.getName());

            int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();

            // resize Image
            BufferedImage resizeImageHintJpg = resizeImageWithHint(originalImage, type, photoMaxWidth);

            if (resizeImageHintJpg != null) {

                // write image back...
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(resizeImageHintJpg, getFormatName(fileData.getName()), baos);
                baos.flush();
                byte[] imageInByte = baos.toByteArray();
                baos.close();

                // update Filedata
                fileData.setContent(imageInByte);

            }
        }

    }

    /**
     * Retruns true if name ends of a known extension. Default is 'jpg'
     * 
     * @param aname
     * @return
     */
    private boolean isPhoto(FileData fileData) {
        if (fileData == null)
            return false;

        if (fileData.getContent().length < 10) {
            return false;
        }
        Pattern pattern = null;
        pattern = Pattern.compile(filePattern);
        if (pattern == null || pattern.matcher(fileData.getName()).find()) {
            return true;
        }

        return false;
    }

    private BufferedImage getImageFromWorkitem(FileData fileData) {
        byte[] fileContent = fileData.getContent();
        if (fileContent != null && fileContent.length > 2) {
            try {

                Iterator<ImageReader> inReaders = ImageIO
                        .getImageReadersByFormatName(getFormatName(fileData.getName()));
                ImageReader imageReader = (ImageReader) inReaders.next();
                ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(fileContent));
                imageReader.setInput(iis);
                BufferedImage originalImage = imageReader.read(0);
                return originalImage;

            } catch (IOException e) {
                logger.severe("ImageInerceptor - unable to load image from workitem : " + e.getMessage());
                e.printStackTrace();
            }
        }

        return null;

    }

    /**
     * Returns the file extension lower cased
     * 
     * @param aFilename
     * @return
     */
    private String getFormatName(String aFilename) {
        if (aFilename.indexOf('.') == -1)
            return null;
        String inFormat = aFilename.substring(aFilename.lastIndexOf('.') + 1);

        return inFormat.toLowerCase();
    }

    /**
     * Reduzes the size of an image.
     * 
     * @param originalImage
     * @param type
     * @param imageMaxWidth
     * @return
     */
    private BufferedImage resizeImageWithHint(BufferedImage originalImage, int type, int imageMaxWidth) {

        // compute hight...
        float width = originalImage.getWidth();
        float height = originalImage.getHeight();
        float factor = (float) width / (float) imageMaxWidth;
        int newHeight = (int) (height / factor);

        BufferedImage resizedImage = new BufferedImage(imageMaxWidth, newHeight, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, imageMaxWidth, newHeight, null);
        g.dispose();
        g.setComposite(AlphaComposite.Src);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        return resizedImage;
    }

}
