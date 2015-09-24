package com.newschip.fingerprint;

import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Read Validity UI settings from xml configuration file.
 */
public class FingerprintConfigReader {

    public static final String TAG = "FingerprintConfigReader";
    public static final String CONFIG_FILE = "/system/etc/ValidityConfig.xml";

    /**
     * Get all configuration settings from xml.
     * @return configuration data {@link ConfigData }
     */
    public static ConfigData getData() {
        ConfigData configData = null;
        try {
    
            InputStream inputStream = new FileInputStream(CONFIG_FILE);
            if (inputStream == null) {
                return configData;
            }
    
            DataHandler dataHandler = new DataHandler();
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            xr.setContentHandler(dataHandler);
            xr.parse(new InputSource(inputStream));
    
            //Read XML content
            configData = dataHandler.getData();
    
            if (inputStream != null) {
                inputStream.close();
            }
    
        } catch (FileNotFoundException fnfe) {
            Log.e(TAG, "Configuration file[" + CONFIG_FILE + "] not found");
        } catch (ParserConfigurationException pce) {
            Log.e(TAG, "SAX parse error", pce);
        } catch (SAXException se) {
            Log.e(TAG, "SAX error", se);
        } catch (IOException ioe) {
            Log.e(TAG, "SAX parse IO error", ioe);
        }

        return configData;
    }


    /**
     * Container class for configuration settings
     */
    public static class ConfigData {

        public int screenOrientation = 0;
        public int sensorType = 1;
        public String disableButton = "0000";
        public boolean showVideo = false;
        public boolean practiceMode = false;
        public boolean hapticFeedback = true;
        public String fingerActionGenericLabel = "Please place";
        public String fingerPlaceOrSwipeLabel = "Place finger";
        public String fingerLiftLabel = "Lift your finger";

        class SensorBar {
            boolean visible = true;
            int xPos = 600;
            int yPos = 800;
            int width = 25;
            int height = 100;
        }

        class FingerSwipeAnimation {
            boolean visible = true;
            int orientation = 0;
            int xPos = 600;
            int yPos = 800;
            float xScale = 1.0f;
            float yScale = 1.0f;
            int offsetLength = 0;
            boolean outlineVisible = true;
            int animationSpeed = 1500;
        }

        class FpDisplay {
            int xPos = 0;
            int yPos = 0;
            int width = 200;
            int height = 400;
            boolean showStartupVideo = true;
        }

        public class DisableButtons {
            public boolean home = false;
            public boolean back = false;
            public boolean menu = false;
            public boolean search = false;
        }

        public SensorBar sensorBar = null;
        public FingerSwipeAnimation fingerSwipeAnimation = null;
        public DisableButtons disableButtons = null;
        public FpDisplay fpDisplay = null;

        ConfigData() {
            sensorBar = new SensorBar();
            fingerSwipeAnimation = new FingerSwipeAnimation();
            fpDisplay = new FpDisplay();
            disableButtons = new DisableButtons();
        }

        public void toStringDebug() {

            Log.i(TAG, "ConfigData():: Screen Orientation=" + screenOrientation
                        + ", SensorType=" + sensorType
                        + ", disabled buttons=" + disableButton
                        + ", fingerPlaceOrSwipeLabel=" + fingerPlaceOrSwipeLabel
                        + ", fingerLiftLabel=" + fingerLiftLabel
                        + ", fingerActionGenericLabel=" + fingerActionGenericLabel
                        + ", showVideo=" + showVideo
                        + ", practiceMode=" + practiceMode
                        + ", hapticFeedback=" + hapticFeedback);

            Log.i(TAG, "Fp Display :: left = " + fpDisplay.xPos
                        + ", top = " + fpDisplay.yPos
                        + ", width = " + fpDisplay.width
                        + ", height = " + fpDisplay.height);

            Log.i(TAG, "DisableButtons =>"
                        + "  home=" + disableButtons.home + ", menu=" + disableButtons.menu
                        + ", search=" + disableButtons.search + ", back=" + disableButtons.back);

            Log.i(TAG, "Finger swipe animation =>"
                        + "  visible=" + fingerSwipeAnimation.visible
                        + ", orientation=" + fingerSwipeAnimation.orientation
                        + ", left=" + fingerSwipeAnimation.xPos
                        + ", top=" + fingerSwipeAnimation.yPos
                        + ", scale width = " + fingerSwipeAnimation.xScale
                        + ", scale height = " + fingerSwipeAnimation.yScale
                        + ", offset length = " + fingerSwipeAnimation.offsetLength
                        + ", outline visible = " + fingerSwipeAnimation.outlineVisible
                        + ", animationSpeed = " + fingerSwipeAnimation.animationSpeed);

            Log.i(TAG, "Fp Display :: left = " + fpDisplay.xPos
                        + ", top = " + fpDisplay.yPos
                        + ", width = " + fpDisplay.width
                        + ", height = " + fpDisplay.height
                        + ", showStartupVideo = " + fpDisplay.showStartupVideo);

        }
    }

    /***********************************************************************
     ************************ SAX Parser ***********************************
     **********************************************************************/
    public static class DataHandler extends DefaultHandler {

        // booleans that check whether it's in a specific tag or not
        private boolean _screenOrientation, _sensorType, _disableButtons, _showVideo,
                _practiceMode, _hapticFeedback, _fingerPlaceOrSwipeLabel, _fingerLiftLabel,
                _fingerActionGenericLabel, _sensorBar, _fingerSwipeAnimation, _fpDisplay;

        // this holds the data
        private ConfigData _data;
    
        /**
         * Returns the data object
         * 
         * @return
         */
        public ConfigData getData() {
            return _data;
        }
    
        /**
         * This gets called when the xml document is first opened
         * 
         * @throws SAXException
         */
        @Override
        public void startDocument() throws SAXException {
            _data = new ConfigData();
        }
    
        /**
         * Called when it's finished handling the document * @throws
         * SAXException
         */
        @Override
        public void endDocument() throws SAXException {
        }
    
        /**
         * This gets called at the start of an element. Here we're also setting
         * the booleans to true if it's at that specific tag.
         * 
         * @param namespaceURI
         * @param localName
         * @param qName
         * @param atts
         * @throws SAXException
         */
        @Override
        public void startElement(String namespaceURI, String localName,
                                    String qName, Attributes atts) throws SAXException {
            if (localName.equals("screenOrientation")) {

                _screenOrientation = true;

            } else if (localName.equals("sensorType")) {

                _sensorType = true;

            } else if (localName.equals("showVideo")) {

                _showVideo = true;

            } else if (localName.equals("practiceMode")) {
                
                _practiceMode = true;

            } else if (localName.equals("hapticFeedback")) {

                _hapticFeedback = true;

            } else if (localName.equals("fingerPlaceOrSwipeLabel")) {

                _fingerPlaceOrSwipeLabel = true;

            } else if (localName.equals("fingerLiftLabel")) {

                _fingerLiftLabel = true;

            } else if (localName.equals("fingerActionGenericLabel")) {

                _fingerActionGenericLabel = true;

            } else if (localName.equals("disableButtons")) {

                _disableButtons = true;

            } else if(localName.equals("sensorBar")) {

                _sensorBar = true;

                if (atts.getIndex("visible") != -1) {
                    _data.sensorBar.visible = Integer.parseInt(atts.getValue("visible")) == 1
                                            ? true : false;
                }

                if (atts.getIndex("xPos") != -1) {
                    _data.sensorBar.xPos = Integer.parseInt(atts.getValue("xPos"));
                }

                if (atts.getIndex("yPos") != -1) {
                    _data.sensorBar.yPos = Integer.parseInt(atts.getValue("yPos"));
                }

                if (atts.getIndex("width") != -1) {
                    _data.sensorBar.width = Integer.parseInt(atts.getValue("width"));
                }

                if (atts.getIndex("height") != -1) {
                    _data.sensorBar.height = Integer.parseInt(atts.getValue("height"));
                }

            } else if (localName.equals("fingerSwipeAnimation")) {

                _fingerSwipeAnimation = true;

                if (atts.getIndex("visible") != -1) {
                    _data.fingerSwipeAnimation.visible =
                                Integer.parseInt(atts.getValue("visible")) == 1 ? true : false;
                }

                if (atts.getIndex("orientation") != -1) {
                    _data.fingerSwipeAnimation.orientation =
                                Integer.parseInt(atts.getValue("orientation"));
                }

                if (atts.getIndex("xPos") != -1) {
                    _data.fingerSwipeAnimation.xPos = Integer.parseInt(atts.getValue("xPos"));
                }

                if (atts.getIndex("yPos") != -1) {
                    _data.fingerSwipeAnimation.yPos = Integer.parseInt(atts.getValue("yPos"));
                }

                if (atts.getIndex("xScale") != -1) {
                    _data.fingerSwipeAnimation.xScale = Float.parseFloat(atts.getValue("xScale"));
                }

                if (atts.getIndex("yScale") != -1) {
                    _data.fingerSwipeAnimation.yScale = Float.parseFloat(atts.getValue("yScale"));
                }

                if (atts.getIndex("offsetLength") != -1) {
                    _data.fingerSwipeAnimation.offsetLength
                            = Integer.parseInt(atts.getValue("offsetLength"));
                }

                if (atts.getIndex("outlineVisible") != -1) {
                    _data.fingerSwipeAnimation.outlineVisible =
                            Integer.parseInt(atts.getValue("outlineVisible")) == 1 ? true : false;
                }

                if (atts.getIndex("animationSpeed") != -1) {
                    _data.fingerSwipeAnimation.animationSpeed
                            = Integer.parseInt(atts.getValue("animationSpeed"));
                }

            } else if (localName.equals("fpDisplay")) {

                _fpDisplay = true;

                if (atts.getIndex("xPos") != -1) {
                    _data.fpDisplay.xPos = Integer.parseInt(atts.getValue("xPos"));
                }

                if (atts.getIndex("yPos") != -1) {
                    _data.fpDisplay.xPos = Integer.parseInt(atts.getValue("yPos"));
                }

                if (atts.getIndex("width") != -1) {
                    _data.fpDisplay.width = Integer.parseInt(atts.getValue("width"));
                }

                if (atts.getIndex("height") != -1) {
                    _data.fpDisplay.height = Integer.parseInt(atts.getValue("height"));
                }

                if (atts.getIndex("showStartupVideo") != -1) {
                    _data.fpDisplay.showStartupVideo = 
                            (Integer.parseInt(atts.getValue("showStartupVideo")) == 1);
                }
            }
        }

        /**
         * Called at the end of the element. Setting the booleans to false, so
         * we know that we've just left that tag.
         * 
         * @param namespaceURI
         * @param localName
         * @param qName
         * @throws SAXException
         */
        @Override
        public void endElement(String namespaceURI, String localName,
            String qName) throws SAXException {
            if (localName.equals("screenOrientation")) {
                _screenOrientation = false;
            } else if (localName.equals("sensorType")) {
                _sensorType = false;
            } else if (localName.equals("showVideo")) {
                _showVideo = false;
            } else if (localName.equals("practiceMode")) {
                _practiceMode = false;
            } else if (localName.equals("hapticFeedback")) {
                _hapticFeedback =false;
            } else if (localName.equals("fingerPlaceOrSwipeLabel")) {
                _fingerPlaceOrSwipeLabel = false;
            } else if (localName.equals("fingerLiftLabel")) {
                _fingerLiftLabel = false;
            } else if (localName.equals("fingerActionGenericLabel")) {
                _fingerActionGenericLabel = false;
            } else if (localName.equals("disableButtons")) {
                _disableButtons = false;
            } else if (localName.equals("sensorBar")) {
                _sensorBar = false;
            } else if (localName.equals("fingerSwipeAnimation")) {
                _fingerSwipeAnimation = false;
            } else if (localName.equals("fpDisplay")) {
                _fpDisplay = false;
            }
        }
    
        /**
         * Calling when we're within an element. Here we're checking to see if
         * there is any content in the tags that we're interested in and
         * populating it in the Config object.
         * 
         * @param ch
         * @param start
         * @param length
         */
        @Override
        public void characters(char ch[], int start, int length) {
            String chars = new String(ch, start, length);
            chars = chars.trim();
            if (_screenOrientation) {
                _data.screenOrientation = Integer.parseInt(chars);
            } else if (_sensorType) {
                _data.sensorType = Integer.parseInt(chars);
            } else if (_showVideo) {
                _data.showVideo = Boolean.parseBoolean(chars);
            } else if (_practiceMode) {
                _data.practiceMode = Boolean.parseBoolean(chars);
            } else if (_hapticFeedback) {
                _data.hapticFeedback =Boolean.parseBoolean(chars);
            } else if (_fingerPlaceOrSwipeLabel) {
                _data.fingerPlaceOrSwipeLabel = chars;
            } else if (_fingerLiftLabel) {
                _data.fingerLiftLabel = chars;
            } else if (_fingerActionGenericLabel) {
                _data.fingerActionGenericLabel = chars;
            } else if (_disableButtons) {
                _data.disableButton = chars;
                _data.disableButtons.search = ch[length-1] == '1' ? true : false;
                _data.disableButtons.home = ch[length-2] == '1' ? true : false;
                _data.disableButtons.back = ch[length-3]  == '1' ? true : false;
                _data.disableButtons.menu = ch[length-4] == '1' ? true : false;
            } else if (_sensorBar) {
                //_data.sensorBar = chars;
            } else if (_fingerSwipeAnimation) {
                //_data.fingerSwipeAnimation = chars;
            } else if (_fpDisplay) {
                //_data.fpDisplay = chars;
            }
        }
    }

}
