/*
 * blue - object composition environment for csound
 * Copyright (C) 2012
 * Steven Yi <stevenyi@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package blue.score.layers.patterns.core;

import blue.score.layers.Layer;
import blue.soundObject.SoundObject;
import blue.utility.ObjectUtilities;
import electric.xml.Element;
import electric.xml.Elements;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author stevenyi
 * 
 */
public class PatternLayer implements Layer {
    
    private SoundObject soundObject = null;
    
    String name = "";
    
    private PatternData patternData = new PatternData();
    
    public SoundObject getSoundObject() {
        return soundObject;
    }

    public void setSoundObject(SoundObject soundObject) {
        this.soundObject = soundObject;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    

    public PatternData getPatternData() {
        return patternData;
    }
    
    public Element saveAsXML() {
        Element retVal = new Element("patternLayer");
        
        if(soundObject != null) {
            retVal.addElement(soundObject.saveAsXML(null));
        }
        retVal.addElement(patternData.saveAsXML());
        retVal.addElement("name", getName());
        
        return retVal;
    }
    
    public static PatternLayer loadFromXML(Element data) {
        PatternLayer layer = new PatternLayer();
        
        Elements nodes = data.getElements();

        int heightIndex = -1;
        
        boolean oldTimeStateValuesFound = false;

        while (nodes.hasMoreElements()) {
            Element node = nodes.next();
            String nodeName = node.getName();

            if ("soundObject".equals(nodeName)) {
                try {
                    layer.setSoundObject((SoundObject)ObjectUtilities.loadFromXML(
                            node));
                } catch (Exception ex) {
                    Logger.getLogger(PatternLayer.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
            } else if ("name".equals(nodeName)) {
                layer.setName(node.getTextString());
            } else if (nodeName.equals("patternData")) {
                layer.patternData = PatternData.loadFromXML(node);
            }
        }
        
        return layer;
    }

    void clearListeners() {
        //
    }
    
}
