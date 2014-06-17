package ve.ucv.ciens.ccg.nxtar.systems;

import ve.ucv.ciens.ccg.nxtar.components.FadeEffectComponent;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

public class FadeEffectRenderingSystem extends EntityProcessingSystem implements Disposable{
	@Mapper ComponentMapper<FadeEffectComponent> fadeMapper;

	private SpriteBatch batch;
	private Texture fadeTexture;
	private OrthographicCamera camera;

	@SuppressWarnings("unchecked")
	public FadeEffectRenderingSystem(){
		super(Aspect.getAspectForAll(FadeEffectComponent.class));

		this.batch = new SpriteBatch();
		this.batch.enableBlending();
		this.camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		Pixmap pixmap = new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Format.RGBA4444);
		pixmap.setColor(1, 1, 1, 1);
		pixmap.fill();
		fadeTexture = new Texture(pixmap);
		pixmap.dispose();
	}

	@Override
	public void dispose(){
		this.fadeTexture.dispose();
		this.batch.dispose();
	}

	@Override
	protected void process(Entity e) {
		FadeEffectComponent fade = fadeMapper.get(e);

		this.batch.setProjectionMatrix(this.camera.combined);
		this.batch.begin();{
			this.batch.setColor(1, 1, 1, fade.getFloatValue());
			this.batch.draw(fadeTexture, -(Gdx.graphics.getWidth() / 2), -(Gdx.graphics.getHeight() / 2));
			this.batch.setColor(1, 1, 1, 1);
		}this.batch.end();
	}
}
