/*
 * Copyright (C) 2014 Miguel Angel Astor Romero
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ve.ucv.ciens.ccg.nxtar.graphics;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

/**
 * <p>Extension of the standard LibGDX perspective camera that allows setting an
 * arbitrary projection matrix when updating.</p>
 */
public class CustomPerspectiveCamera extends PerspectiveCamera{
	private final Vector3 tmp = new Vector3();

	public CustomPerspectiveCamera(float fieldOfView, float viewportWidth, float viewportHeight){
		super(fieldOfView, viewportWidth, viewportHeight);
		update();
	}

	public void update(Matrix4 customProjection){
		this.update(customProjection, true);
	}

	public void update(Matrix4 customProjection, boolean updateFrustum){
		projection.set(customProjection);
		view.setToLookAt(position, tmp.set(position).add(direction), up);
		combined.set(projection).mul(view);

		if(updateFrustum){
			invProjectionView.set(combined).inv();
			frustum.update(invProjectionView);
		}
	}

	public void setCustomARProjectionMatrix(final float focalPointX, final float focalPointY, final float cameraCenterX, final float cameraCenterY, final float near, final float far, final float w, final float h){
		final float FAR_PLUS_NEAR = far + near;
		final float FAR_LESS_NEAR = far - near;

		projection.val[Matrix4.M00] = -2.0f * focalPointX / w;
		projection.val[Matrix4.M10] = 0.0f;
		projection.val[Matrix4.M20] = 0.0f;
		projection.val[Matrix4.M30] = 0.0f;

		projection.val[Matrix4.M01] = 0.0f;
		projection.val[Matrix4.M11] = 2.0f * focalPointY / h;
		projection.val[Matrix4.M21] = 0.0f;
		projection.val[Matrix4.M31] = 0.0f;

		projection.val[Matrix4.M02] = 2.0f * cameraCenterX / w - 1.0f;
		projection.val[Matrix4.M12] = 2.0f * cameraCenterY / h - 1.0f;
		projection.val[Matrix4.M22] = -FAR_PLUS_NEAR / FAR_LESS_NEAR;
		projection.val[Matrix4.M32] = -1.0f;

		projection.val[Matrix4.M03] = 0.0f;
		projection.val[Matrix4.M13] = 0.0f;
		projection.val[Matrix4.M23] = -2.0f * far * near / FAR_LESS_NEAR;
		projection.val[Matrix4.M33] = 0.0f;
	}
}
