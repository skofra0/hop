/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.apache.hop.trans;

import org.apache.hop.core.gui.AreaOwner;
import org.apache.hop.core.gui.GCInterface;
import org.apache.hop.core.gui.Point;
import org.apache.hop.trans.step.StepMeta;

import java.util.List;

public class TransPainterFlyoutExtension {

  public GCInterface gc;
  public boolean shadow;
  public List<AreaOwner> areaOwners;
  public TransMeta transMeta;
  public StepMeta stepMeta;
  public Point offset;
  public Point area;
  public float translationX;
  public float translationY;
  public float magnification;

  public TransPainterFlyoutExtension( GCInterface gc, List<AreaOwner> areaOwners, TransMeta transMeta,
                                      StepMeta stepMeta, float translationX, float translationY, float magnification, Point area, Point offset ) {
    super();
    this.gc = gc;
    this.areaOwners = areaOwners;
    this.transMeta = transMeta;
    this.stepMeta = stepMeta;
    this.translationX = translationX;
    this.translationY = translationY;
    this.magnification = magnification;
    this.area = area;
    this.offset = offset;
  }
}
