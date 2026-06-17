package org.vmstudio.visor.compatibility.iris.extensions;

import org.vmstudio.visor.api.client.render.VRRenderPass;

public interface IrisPipelineManagerExtension {

    Object visor$getPassPipeline(VRRenderPass pass);

    Object visor$getBasePipeline();
}