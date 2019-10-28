/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.rockbite.tools.talos.editor.widgets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.utils.CameraController;

public abstract class ViewportWidget extends Table {

    protected OrthographicCamera camera;

    protected Matrix4 emptyTransform = new Matrix4();
    private Matrix4 prevTransform  = new Matrix4();
    private Matrix4 prevProjection = new Matrix4();

    public CameraController cameraController;

    public ViewportWidget() {
        camera = new OrthographicCamera();
        camera.viewportWidth = 7;


        setTouchable(Touchable.enabled);


        cameraController = new CameraController(camera);
        cameraController.setInvert(true);

        addListener(new InputListener() {
            @Override
            public boolean scrolled (InputEvent event, float x, float y, int amount) {
                camera.zoom += amount * 0.5f;
                camera.zoom = MathUtils.clamp(camera.zoom, 0.1f, 100f);
                camera.update();
                cameraScrolledWithAmount(amount);

                return true;
            }

            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                cameraController.touchDown((int)x, (int)y, pointer, button);
                return true;
            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                cameraController.touchUp((int)x, (int)y, pointer, button);
            }

            @Override
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                cameraController.touchDragged((int)x, (int)y, pointer);
            }

            @Override
            public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                TalosMain.Instance().UIStage().getStage().setScrollFocus(ViewportWidget.this);
            }

            @Override
            public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                if (pointer != -1) return; //Only care about exit/enter from mouse move
                TalosMain.Instance().UIStage().getStage().setScrollFocus(null);
            }
        });

    }

    Vector2 temp = new Vector2();

    protected void cameraScrolledWithAmount (int amount) {

    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();

        localToScreenCoordinates(temp.set(getX(), getY()));
        int x = (int)temp.x;
        int y = (int)temp.y;

        localToScreenCoordinates(temp.set(getX() + getWidth(), getY() + getHeight()));

        int x2 = (int)temp.x;
        int y2 = (int)temp.y;

        int ssWidth = x2 - x;
        int ssHeight = y - y2;

        HdpiUtils.glViewport(x, Gdx.graphics.getHeight() - y, ssWidth, ssHeight);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float aspect = getWidth()/getHeight();

        camera.viewportHeight = camera.viewportWidth / aspect;

        camera.update();

        prevTransform.set(batch.getTransformMatrix());
        prevProjection.set(batch.getProjectionMatrix());
        batch.setProjectionMatrix(camera.combined);
        batch.setTransformMatrix(emptyTransform);

        batch.begin();
        drawContent(batch, parentAlpha);
        batch.end();

        HdpiUtils.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.setProjectionMatrix(prevProjection);
        batch.setTransformMatrix(prevTransform);
        batch.begin();

        super.draw(batch, parentAlpha);
    }

    protected void drawGroup(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    public abstract void drawContent(Batch batch, float parentAlpha);

    public float getCameraPosX() {
        return camera.position.x;
    }

    public float getCameraPosY() {
        return camera.position.y;
    }

    public float getCameraZoom() {
        return camera.zoom;
    }

    public void setCameraPos(float x, float y) {
        camera.position.set(x, y, 0);
    }

    public void setCameraZoom(float zoom) {
        camera.zoom = zoom;
    }
}
