package org.vmstudio.visor.core.client.render.target;



public interface RenderTargetHolder {


    void init(int width, int height) throws Exception;
    void resize(int width, int height) throws Exception;

    void destroy();

}
