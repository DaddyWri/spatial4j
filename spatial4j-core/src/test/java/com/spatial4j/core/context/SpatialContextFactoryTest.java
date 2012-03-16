/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.spatial4j.core.context;

import com.spatial4j.core.context.simple.SimpleSpatialContext;
import com.spatial4j.core.distance.CartesianDistCalc;
import com.spatial4j.core.distance.GeodesicSphereDistCalc;
import com.spatial4j.core.distance.GeodesicSphereDistCalc.Haversine;
import com.spatial4j.core.shape.simple.RectangleImpl;
import com.spatial4j.proj4j.datum.Datum;

import org.junit.After;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;


public class SpatialContextFactoryTest {
  public static final String PROP = "SpatialContextFactory";

  @After
  public void tearDown() {
    System.getProperties().remove(PROP);
  }
  
  private SpatialContext call(String... argsStr) {
    Map<String,String> args = new HashMap<String,String>();
    for (int i = 0; i < argsStr.length; i+=2) {
      String key = argsStr[i];
      String val = argsStr[i+1];
      args.put(key,val);
    }
    return SpatialContextFactory.makeSpatialContext(args, getClass().getClassLoader());
  }
  
  @Test
  public void testDefault() {
    SpatialContext ctx = call();//default
    assertEquals(SimpleSpatialContext.class,ctx.getClass());
    assertEquals(Datum.WGS84,ctx.getCRS().getDatum());
    assertEquals(Haversine.class,ctx.getDistCalc().getClass());
  }
  
  @Test
  public void testCustom() {
    SpatialContext sc = call("units","u",
        "distCalculator","cartesian^2",
        "worldBounds","-100 0 75 200");//West South East North
    assertEquals(new CartesianDistCalc(true),sc.getDistCalc());
    assertEquals(new RectangleImpl(-100,75,0,200),sc.getWorldBounds());

    sc = call("units","miles",
        "distCalculator","lawOfCosines");
    assertEquals(new GeodesicSphereDistCalc.LawOfCosines(Datum.WGS84.getEllipsoid().getEquatorRadius()),
        sc.getDistCalc());
  }
  
  @Test
  public void testSystemPropertyLookup() {
    System.setProperty(PROP,DSCF.class.getName());
    
    assertEquals(new RectangleImpl(0,100,0,100),call().getWorldBounds());//DSCF returns this
  }

  public static class DSCF extends SpatialContextFactory {

    @Override
    protected SpatialContext newSpatialContext() {
      return new SimpleSpatialContext(new RectangleImpl(0,100,0,100),null);
    }
  }
}
