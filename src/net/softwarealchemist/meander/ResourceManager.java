package net.softwarealchemist.meander;

import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;

import com.threed.jpct.Loader;
import com.threed.jpct.Object3D;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;

public class ResourceManager {
	AssetManager assManager;
	
	public ResourceManager(AssetManager assManager) {
		this.assManager = assManager;
	}

	public Object3D loadModelWithTexture(String modelName) {
		Object3D model;
		loadTexture(modelName);
		model = loadModel(modelName);
		model.setTexture(modelName);
		return model;
	}

	private Object3D loadModel(String modelName) {
		Object3D model = null;
		try {	
			InputStream objStream, mtlStream;
			objStream= assManager.open("models/"+modelName+".obj");
			mtlStream = assManager.open("models/"+modelName+".mtl");
			model = Loader.loadOBJ(objStream, mtlStream, 1)[0];
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return model;
	}

	public void loadTexture(String textureName) {
		try {
			Texture texture = new Texture(assManager.open("textures/"+textureName+".png"), true);
			texture.setFiltering(false);
			TextureManager.getInstance().addTexture(textureName, texture);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
