package ve.ucv.ciens.ccg.nxtar.systems;

import ve.ucv.ciens.ccg.nxtar.components.GeometryComponent;
import ve.ucv.ciens.ccg.nxtar.components.MarkerCodeComponent;
import ve.ucv.ciens.ccg.nxtar.components.MeshComponent;
import ve.ucv.ciens.ccg.nxtar.components.ShaderComponent;
import ve.ucv.ciens.ccg.nxtar.graphics.RenderParameters;
import ve.ucv.ciens.ccg.nxtar.interfaces.ImageProcessor.MarkerData;
import ve.ucv.ciens.ccg.nxtar.utils.ProjectConstants;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Matrix4;

public class MarkerRenderingSystem extends EntityProcessingSystem {
	@Mapper ComponentMapper<MarkerCodeComponent> markerMapper;
	@Mapper ComponentMapper<GeometryComponent> geometryMapper;
	@Mapper ComponentMapper<ShaderComponent> shaderMapper;
	@Mapper ComponentMapper<MeshComponent> meshMapper;

	private static final String TAG = "MARKER_RENDERING_SYSTEM";
	private static final String CLASS_NAME = MarkerRenderingSystem.class.getSimpleName();

	/**
	 * <p>A matrix representing 3D translations.</p>
	 */
	private Matrix4 translationMatrix;

	/**
	 * <p>A matrix representing 3D rotations.</p>
	 */
	private Matrix4 rotationMatrix;

	/**
	 * <p>A matrix representing 3D scalings.</p>
	 */
	private Matrix4 scalingMatrix;

	/**
	 * <p>The total transformation to be applied to an entity.</p>
	 */
	private Matrix4 combinedTransformationMatrix;

	MarkerData markers;

	@SuppressWarnings("unchecked")
	public MarkerRenderingSystem(){
		super(Aspect.getAspectForAll(MarkerCodeComponent.class, GeometryComponent.class, ShaderComponent.class, MeshComponent.class));

		markers = null;
		translationMatrix = new Matrix4().setToTranslation(0.0f, 0.0f, 0.0f);
		rotationMatrix = new Matrix4().idt();
		scalingMatrix = new Matrix4().setToScaling(0.0f, 0.0f, 0.0f);
		combinedTransformationMatrix = new Matrix4();
	}

	public void setMarkerData(MarkerData markers){
		this.markers = markers;
	}

	@Override
	protected void process(Entity e) {
		MarkerCodeComponent marker;
		GeometryComponent geometry;
		ShaderComponent shaderComp;
		MeshComponent meshComp;

		if(markers == null)
			return;

		Gdx.app.log(TAG, CLASS_NAME + ".process(): Getting components.");
		marker = markerMapper.get(e);
		geometry = geometryMapper.get(e);
		shaderComp = shaderMapper.get(e);
		meshComp = meshMapper.get(e);

		Gdx.app.log(TAG, CLASS_NAME + ".process(): Processing markers.");
		for(int i = 0; i < ProjectConstants.MAXIMUM_NUMBER_OF_MARKERS; i++){
			if(markers.markerCodes[i] != 1){
				if(markers.markerCodes[i] == marker.code){
					Gdx.app.log(TAG, CLASS_NAME + ".process(): Rendering marker code " + Integer.toString(markers.markerCodes[i]) + ".");
					// Set the geometric transformations.
					translationMatrix.setToTranslation(geometry.position);
					rotationMatrix.set(geometry.rotation);
					scalingMatrix.setToScaling(geometry.scaling);
					combinedTransformationMatrix.idt().mul(scalingMatrix).mul(rotationMatrix).mul(translationMatrix);
					RenderParameters.setTransformationMatrix(combinedTransformationMatrix);

					// Render the marker;
					shaderComp.shader.getShaderProgram().begin();{
						shaderComp.shader.setUniforms();
						meshComp.model.render(shaderComp.shader.getShaderProgram(), GL20.GL_TRIANGLES);
					}shaderComp.shader.getShaderProgram().end();
				}
			}else{
				Gdx.app.log(TAG, CLASS_NAME + ".process(): Skipping marker number " + Integer.toString(i) + ".");
			}
		}
		
		markers = null;
	}

}
