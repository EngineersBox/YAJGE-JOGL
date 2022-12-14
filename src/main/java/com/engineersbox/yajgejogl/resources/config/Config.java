package com.engineersbox.yajgejogl.resources.config;

public class Config {
  public final Config.Engine engine;
  public final Config.Render render;
  public final Config.Sound sound;
  public final Config.Video video;
  // NOTE: incomplete #62 implementation
  public enum GraphicsAPIType {
    OPENGL,
    VULKAN;
  }

  public static class Engine {
    public final Engine.Debug debug;
    public final Engine.Features features;
    public final Engine.GlOptions glOptions;
    public final Engine.Resources resources;

    public static class Debug {
      public final boolean depthOnly;
      public final boolean flat;
      public final boolean overlay;
      public final boolean shadowsOnly;
      public final boolean showAxis;
      public final boolean showCascades;

      public Debug(
          com.typesafe.config.Config c,
          java.lang.String parentPath,
          $TsCfgValidator $tsCfgValidator) {
        this.depthOnly = c.hasPathOrNull("depthOnly") && c.getBoolean("depthOnly");
        this.flat = c.hasPathOrNull("flat") && c.getBoolean("flat");
        this.overlay = c.hasPathOrNull("overlay") && c.getBoolean("overlay");
        this.shadowsOnly = c.hasPathOrNull("shadowsOnly") && c.getBoolean("shadowsOnly");
        this.showAxis = c.hasPathOrNull("showAxis") && c.getBoolean("showAxis");
        this.showCascades = c.hasPathOrNull("showCascades") && c.getBoolean("showCascades");
      }
    }

    public static class Features {
      public final GraphicsAPIType graphicsAPI;

      public Features(
          com.typesafe.config.Config c,
          java.lang.String parentPath,
          $TsCfgValidator $tsCfgValidator) {
        this.graphicsAPI = GraphicsAPIType.valueOf(c.getString("graphicsAPI"));
      }
    }

    public static class GlOptions {
      public final boolean antialiasing;
      public final boolean compatProfile;
      public final boolean cullface;
      public final boolean logs;
      public final boolean showTrianges;

      public GlOptions(
          com.typesafe.config.Config c,
          java.lang.String parentPath,
          $TsCfgValidator $tsCfgValidator) {
        this.antialiasing = !c.hasPathOrNull("antialiasing") || c.getBoolean("antialiasing");
        this.compatProfile = !c.hasPathOrNull("compatProfile") || c.getBoolean("compatProfile");
        this.cullface = !c.hasPathOrNull("cullface") || c.getBoolean("cullface");
        this.logs = c.hasPathOrNull("logs") && c.getBoolean("logs");
        this.showTrianges = c.hasPathOrNull("showTrianges") && c.getBoolean("showTrianges");
      }
    }

    public static class Resources {
      public final java.lang.String shaders;
      public final java.lang.String textures;

      public Resources(
          com.typesafe.config.Config c,
          java.lang.String parentPath,
          $TsCfgValidator $tsCfgValidator) {
        this.shaders = $_reqStr(parentPath, c, "shaders", $tsCfgValidator);
        this.textures = $_reqStr(parentPath, c, "textures", $tsCfgValidator);
      }

      private static java.lang.String $_reqStr(
          java.lang.String parentPath,
          com.typesafe.config.Config c,
          java.lang.String path,
          $TsCfgValidator $tsCfgValidator) {
        if (c == null) return null;
        try {
          return c.getString(path);
        } catch (com.typesafe.config.ConfigException e) {
          $tsCfgValidator.addBadPath(parentPath + path, e);
          return null;
        }
      }
    }

    public Engine(
        com.typesafe.config.Config c,
        java.lang.String parentPath,
        $TsCfgValidator $tsCfgValidator) {
      this.debug =
          c.hasPathOrNull("debug")
              ? new Engine.Debug(c.getConfig("debug"), parentPath + "debug.", $tsCfgValidator)
              : new Engine.Debug(
                  com.typesafe.config.ConfigFactory.parseString("debug{}"),
                  parentPath + "debug.",
                  $tsCfgValidator);
      this.features =
          c.hasPathOrNull("features")
              ? new Engine.Features(
                  c.getConfig("features"), parentPath + "features.", $tsCfgValidator)
              : new Engine.Features(
                  com.typesafe.config.ConfigFactory.parseString("features{}"),
                  parentPath + "features.",
                  $tsCfgValidator);
      this.glOptions =
          c.hasPathOrNull("glOptions")
              ? new Engine.GlOptions(
                  c.getConfig("glOptions"), parentPath + "glOptions.", $tsCfgValidator)
              : new Engine.GlOptions(
                  com.typesafe.config.ConfigFactory.parseString("glOptions{}"),
                  parentPath + "glOptions.",
                  $tsCfgValidator);
      this.resources =
          c.hasPathOrNull("resources")
              ? new Engine.Resources(
                  c.getConfig("resources"), parentPath + "resources.", $tsCfgValidator)
              : new Engine.Resources(
                  com.typesafe.config.ConfigFactory.parseString("resources{}"),
                  parentPath + "resources.",
                  $tsCfgValidator);
    }
  }

  public static class Render {
    public final Render.Camera camera;
    public final Render.Lighting lighting;

    public static class Camera {
      public final double fov;
      public final boolean frustrumCulling;
      public final double zFar;
      public final double zNear;

      public Camera(
          com.typesafe.config.Config c,
          java.lang.String parentPath,
          $TsCfgValidator $tsCfgValidator) {
        this.fov = c.hasPathOrNull("fov") ? c.getDouble("fov") : 60.0;
        this.frustrumCulling =
            !c.hasPathOrNull("frustrumCulling") || c.getBoolean("frustrumCulling");
        this.zFar = c.hasPathOrNull("zFar") ? c.getDouble("zFar") : 1000.0;
        this.zNear = c.hasPathOrNull("zNear") ? c.getDouble("zNear") : 0.01;
      }
    }

    public static class Lighting {
      public final int maxPointLights;
      public final int maxSpotLights;
      public final int shadowMapHeight;
      public final int shadowMapWidth;

      public Lighting(
          com.typesafe.config.Config c,
          java.lang.String parentPath,
          $TsCfgValidator $tsCfgValidator) {
        this.maxPointLights = c.hasPathOrNull("maxPointLights") ? c.getInt("maxPointLights") : 5;
        this.maxSpotLights = c.hasPathOrNull("maxSpotLights") ? c.getInt("maxSpotLights") : 5;
        this.shadowMapHeight =
            c.hasPathOrNull("shadowMapHeight") ? c.getInt("shadowMapHeight") : 1024;
        this.shadowMapWidth = c.hasPathOrNull("shadowMapWidth") ? c.getInt("shadowMapWidth") : 1024;
      }
    }

    public Render(
        com.typesafe.config.Config c,
        java.lang.String parentPath,
        $TsCfgValidator $tsCfgValidator) {
      this.camera =
          c.hasPathOrNull("camera")
              ? new Render.Camera(c.getConfig("camera"), parentPath + "camera.", $tsCfgValidator)
              : new Render.Camera(
                  com.typesafe.config.ConfigFactory.parseString("camera{}"),
                  parentPath + "camera.",
                  $tsCfgValidator);
      this.lighting =
          c.hasPathOrNull("lighting")
              ? new Render.Lighting(
                  c.getConfig("lighting"), parentPath + "lighting.", $tsCfgValidator)
              : new Render.Lighting(
                  com.typesafe.config.ConfigFactory.parseString("lighting{}"),
                  parentPath + "lighting.",
                  $tsCfgValidator);
    }
  }

  public static class Sound {
    public final double effects;
    public final double master;
    public final double music;

    public Sound(
        com.typesafe.config.Config c,
        java.lang.String parentPath,
        $TsCfgValidator $tsCfgValidator) {
      this.effects = c.hasPathOrNull("effects") ? c.getDouble("effects") : 1.0;
      this.master = c.hasPathOrNull("master") ? c.getDouble("master") : 1.0;
      this.music = c.hasPathOrNull("music") ? c.getDouble("music") : 1.0;
    }
  }

  public static class Video {
    public final int fps;
    public final int monitor;
    public final Video.Resolution resolution;
    public final boolean showFps;
    public final int ups;
    public final boolean vsync;

    public static class Resolution {
      public final int height;
      public final int width;

      public Resolution(
          com.typesafe.config.Config c,
          java.lang.String parentPath,
          $TsCfgValidator $tsCfgValidator) {
        this.height = c.hasPathOrNull("height") ? c.getInt("height") : 600;
        this.width = c.hasPathOrNull("width") ? c.getInt("width") : 800;
      }
    }

    public Video(
        com.typesafe.config.Config c,
        java.lang.String parentPath,
        $TsCfgValidator $tsCfgValidator) {
      this.fps = c.hasPathOrNull("fps") ? c.getInt("fps") : 60;
      this.monitor = c.hasPathOrNull("monitor") ? c.getInt("monitor") : 0;
      this.resolution =
          c.hasPathOrNull("resolution")
              ? new Video.Resolution(
                  c.getConfig("resolution"), parentPath + "resolution.", $tsCfgValidator)
              : new Video.Resolution(
                  com.typesafe.config.ConfigFactory.parseString("resolution{}"),
                  parentPath + "resolution.",
                  $tsCfgValidator);
      this.showFps = !c.hasPathOrNull("showFps") || c.getBoolean("showFps");
      this.ups = c.hasPathOrNull("ups") ? c.getInt("ups") : 30;
      this.vsync = !c.hasPathOrNull("vsync") || c.getBoolean("vsync");
    }
  }

  public Config(com.typesafe.config.Config c) {
    final $TsCfgValidator $tsCfgValidator = new $TsCfgValidator();
    final java.lang.String parentPath = "";
    this.engine =
        c.hasPathOrNull("engine")
            ? new Config.Engine(c.getConfig("engine"), parentPath + "engine.", $tsCfgValidator)
            : new Config.Engine(
                com.typesafe.config.ConfigFactory.parseString("engine{}"),
                parentPath + "engine.",
                $tsCfgValidator);
    this.render =
        c.hasPathOrNull("render")
            ? new Config.Render(c.getConfig("render"), parentPath + "render.", $tsCfgValidator)
            : new Config.Render(
                com.typesafe.config.ConfigFactory.parseString("render{}"),
                parentPath + "render.",
                $tsCfgValidator);
    this.sound =
        c.hasPathOrNull("sound")
            ? new Config.Sound(c.getConfig("sound"), parentPath + "sound.", $tsCfgValidator)
            : new Config.Sound(
                com.typesafe.config.ConfigFactory.parseString("sound{}"),
                parentPath + "sound.",
                $tsCfgValidator);
    this.video =
        c.hasPathOrNull("video")
            ? new Config.Video(c.getConfig("video"), parentPath + "video.", $tsCfgValidator)
            : new Config.Video(
                com.typesafe.config.ConfigFactory.parseString("video{}"),
                parentPath + "video.",
                $tsCfgValidator);
    $tsCfgValidator.validate();
  }

  private static final class $TsCfgValidator {
    private final java.util.List<java.lang.String> badPaths = new java.util.ArrayList<>();

    void addBadPath(java.lang.String path, com.typesafe.config.ConfigException e) {
      badPaths.add("'" + path + "': " + e.getClass().getName() + "(" + e.getMessage() + ")");
    }

    void validate() {
      if (!badPaths.isEmpty()) {
        java.lang.StringBuilder sb = new java.lang.StringBuilder("Invalid configuration:");
        for (java.lang.String path : badPaths) {
          sb.append("\n    ").append(path);
        }
        throw new com.typesafe.config.ConfigException(sb.toString()) {};
      }
    }
  }
}