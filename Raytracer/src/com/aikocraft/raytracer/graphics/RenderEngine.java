 package com.aikocraft.raytracer.graphics;

import java.lang.management.LockInfo;
import java.util.ArrayList;
import java.util.Arrays;

import com.aikocraft.coffee.math.vectors.Vec3;
import com.aikocraft.raytracer.Game;

public class RenderEngine {
	public static RenderEngine i;
	public static float nearPlane = 0.1f;
	public static float farPlane = 15f;
	public static int npw;
	public static int nph;
	
	public static float fov;
	
	public Sphere s1 = new Sphere(new Vec3(-12, 0, 0), 8f, new Vec3((float)Math.random(), (float)Math.random(), (float)Math.random()));
	public Sphere s2 = new Sphere(new Vec3(+12, 0, 0), 8f, new Vec3((float)Math.random(), (float)Math.random(), (float)Math.random()));
	public Sphere s3 = new Sphere(new Vec3(0, -12, 0), 8f, new Vec3((float)Math.random(), (float)Math.random(), (float)Math.random()));
	public Sphere s4 = new Sphere(new Vec3(0, +12, 0), 8f, new Vec3((float)Math.random(), (float)Math.random(), (float)Math.random()));
	public Sphere s5 = new Sphere(new Vec3(0, 0, -12), 8f, new Vec3((float)Math.random(), (float)Math.random(), (float)Math.random()));
	public Sphere s6 = new Sphere(new Vec3(0, 0, +12), 8f, new Vec3((float)Math.random(), (float)Math.random(), (float)Math.random()));
	public Sphere s7 = new Sphere(new Vec3(0, 0, 0), 0.5f, new Vec3((float)Math.random(), (float)Math.random(), (float)Math.random()));

	
	public Plane p1 = new Plane();
	
	public Light l1 = new Light(new Vec3(0, 0, 6), 0.001f, 0.05f, 0.0f);
	
	public static ArrayList<Geometry> geoms = new ArrayList<Geometry>();
	public static ArrayList<Light> lights = new ArrayList<Light>();
	public static boolean screenshot;
	public static Thread mainThread;
	public int[] pixels;
	
	private RenderThread rt1;
	private RenderThread rt2;
	private RenderThread rt3;
	private RenderThread rt4;
	
	public Object lock = new Object();
	
	public RenderEngine(int w, int h, int res, int npw, int nph, int[] pixels) {
		RenderEngine.npw = npw;
		RenderEngine.nph = nph;
		i = this;
		this.pixels = pixels;
		
		rt1 = new RenderThread(0, 0);
		rt1.start();
		
		rt2 = new RenderThread(1, 0);
		rt2.start();
		
		rt3 = new RenderThread(0, 1);
		rt3.start();
		
		rt4 = new RenderThread(1, 1);
		rt4.start();	
	}

	public void render() {	
		l1.quadAtt = 0.01f;
		l1.linAtt = 0.0f;
		l1.constAtt = 1.0f;
		
		RenderEngine.mainThread = Thread.currentThread();
		Arrays.fill(pixels, 0x00000);		
		
		if (screenshot)
			System.out.println(">SCREENSHOT\nwidth: " + npw + "\nheight: " + nph);	
		
		synchronized (lock) {			
			lock.notifyAll();		
		}
		
		while (rt1.getState() != Thread.State.WAITING || rt2.getState() != Thread.State.WAITING 
				|| rt3.getState() != Thread.State.WAITING || rt4.getState() != Thread.State.WAITING) {
			try {
				if (screenshot) {
					Thread.sleep(3000);
					
					for (int xp = 0; xp < npw/2; xp++)
						for (int yp = 0; yp < nph/2; yp++)
							pixels[xp + yp * npw] = rt1.pixels[xp + yp * npw/2];
					
					for (int xp = npw/2; xp < npw; xp++)
						for (int yp = 0; yp < nph/2; yp++)
							pixels[xp + yp * npw] = rt2.pixels[(xp-npw/2) + yp * npw/2];	
					
					for (int xp = 0; xp < npw/2; xp++)
						for (int yp = npw/2; yp < nph; yp++)
							pixels[xp + yp * npw] = rt3.pixels[xp + (yp-nph/2) * npw/2];	
					
					for (int xp = npw/2; xp < npw; xp++)
						for (int yp = npw/2; yp < nph; yp++)
							pixels[xp + yp * npw] = rt4.pixels[(xp-npw/2) + (yp-nph/2) * npw/2];	
					
					Game.i.renderBuffer();					
				} else {
					Thread.sleep(1);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		for (int xp = 0; xp < npw/2; xp++)
			for (int yp = 0; yp < nph/2; yp++)
				pixels[xp + yp * npw] = rt1.pixels[xp + yp * npw/2];
		for (int xp = npw/2; xp < npw; xp++)
			for (int yp = 0; yp < nph/2; yp++)
				pixels[xp + yp * npw] = rt2.pixels[(xp-npw/2) + yp * npw/2];	
		for (int xp = 0; xp < npw/2; xp++)
			for (int yp = nph/2; yp < nph; yp++)
				pixels[xp + yp * npw] = rt3.pixels[xp + (yp-nph/2) * npw/2];	
		for (int xp = npw/2; xp < npw; xp++)
			for (int yp = npw/2; yp < nph; yp++)
				pixels[xp + yp * npw] = rt4.pixels[(xp-npw/2) + (yp-nph/2) * npw/2];	
	}
	
	public static int rgbToHex(int r, int g, int b) {
		return (b) | (g << 8) | (r << 16);
	}

	public void onResize() {
		rt1.onResize();
		rt2.onResize();
		rt3.onResize();
		rt4.onResize();
	}
}