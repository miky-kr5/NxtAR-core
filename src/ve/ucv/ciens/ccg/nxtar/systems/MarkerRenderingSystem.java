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
		GeometryComponent   geometry;
		ShaderComponent     shaderComp;
		MeshComponent       meshComp;

		if(markers == null)
			return;

		Gdx.app.log(TAG, CLASS_NAME + ".process(): Getting components.");
		marker     = markerMapper.get(e);
		geometry   = geometryMapper.get(e);
		shaderComp = shaderMapper.get(e);
		meshComp   = meshMapper.get(e);

		Gdx.app.log(TAG, CLASS_NAME + ".process(): Processing markers.");
		for(int i = 0; i < ProjectConstants.MAXIMUM_NUMBER_OF_MARKERS; i++){
			if(markers.markerCodes[i] != 1){
				if(markers.markerCodes[i] == marker.code){
					Gdx.app.log(TAG, CLASS_NAME + ".process(): Rendering marker code " + Integer.toString(markers.markerCodes[i]) + ".");
					// Set the geometric transformations.
					translationMatrix.setToTranslation(geometry.position);

					rotationMatrix.val[0] = geometry.rotation.val[0];
					rotationMatrix.val[1] = geometry.rotation.val[1];
					rotationMatrix.val[2] = geometry.rotation.val[2];
					rotationMatrix.val[3] = 0;
					rotationMatrix.val[4] = geometry.rotation.val[3];
					rotationMatrix.val[5] = geometry.rotation.val[4];
					rotationMatrix.val[6] = geometry.rotation.val[5];
					rotationMatrix.val[7] = 0;
					rotationMatrix.val[8] = geometry.rotation.val[6];
					rotationMatrix.val[9] = geometry.rotation.val[7];
					rotationMatrix.val[10] = geometry.rotation.val[8];
					rotationMatrix.val[11] = 0;
					rotationMatrix.val[12] = 0;
					rotationMatrix.val[13] = 0;
					rotationMatrix.val[14] = 0;
					rotationMatrix.val[15] = 1;

					scalingMatrix.setToScaling(geometry.scaling);
					combinedTransformationMatrix.idt().mul(translationMatrix).mul(rotationMatrix).mul(scalingMatrix);
					RenderParameters.setTransformationMatrix(combinedTransformationMatrix);

					// Render the marker;
					shaderComp.shader.getShaderProgram().begin();{
						shaderComp.shader.setUniforms();
						meshComp.model.render(shaderComp.shader.getShaderProgram(), GL20.GL_TRIANGLES);
					}shaderComp.shader.getShaderProgram().end();

					break;
				}
			}else{
				Gdx.app.log(TAG, CLASS_NAME + ".process(): Skipping marker number " + Integer.toString(i) + ".");
			}
		}

		markers = null;
	}

}
