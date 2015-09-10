package com.learning.kirisanth.brickbreaker;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Kirisanth on 9/1/2015.
 * Followed Tutorial from following site
 * http://www.learnopengles.com/android-lesson-one-getting-started/
 */
public class MainRenderer implements GLSurfaceView.Renderer{
    // New class members
    private static final String TAG = "MyGLRenderer";
    /** Store our model data in a float buffer. */
    private final FloatBuffer mTriangle1Vertices;
    private final FloatBuffer mTriangle2Vertices;
    private final FloatBuffer mTriangle3Vertices;

    private final FloatBuffer mSquareVertices;
    private final ShortBuffer drawListBuffer;

    // New class definitions
    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    private float[] mViewMatrix = new float[16];


    //TODO: Why are we hardcoding this to 4
    /** How many bytes per float. */
    private final int mBytesPerFloat = 4;

    //New class members
    /** This will be used to pass in the transformation matrix. */
    private int mMVPMatrixHandle;

    /** This will be used to pass in model position information. */
    private int mPositionHandle;

    /** This will be used to pass in model color information. */
    private int mColorHandle;

    // New class members
    /** Store the projection matrix. This is used to project the scene onto a 2D viewport. */
    private float[] mProjectionMatrix = new float[16];



    private float[] mModelMatrix = new float[16];


    // New class members
    /** Allocate storage for the final combined matrix. This will be passed into the shader program. */
    private float[] mMVPMatrix = new float[16];

    /** How many elements per vertex. */
    private final int mStrideBytes = 7 * mBytesPerFloat;

    /** Offset of the position data. */
    private final int mPositionOffset = 0;

    /** Size of the position data in elements. */
    private final int mPositionDataSize = 3;

    /** Offset of the color data. */
    private final int mColorOffset = 3;

    /** Size of the color data in elements. */
    private final int mColorDataSize = 4;

    private Square mSquare;


    //Constructor
    public MainRenderer() {
        // I believe this is ES1
        // This triangle is red, green, and blue.
        final float[] triangle1VerticesData = {
                // X Y, Z,
                // R, G, B, A
                -0.5f, -0.25f, 0.0f, //One set of points
                1.0f, 0.0f, 0.0f, 1.0f,

                0.5f, -0.25f, 0.0f, //Second set of points
                0.0f, 0.0f, 1.0f, 1.0f,

                0.0f, 0.559016994f, 0.0f, //Third set of points
                0.0f, 1.0f, 0.0f, 1.0f
        };

        // This triangle is yellow, cyan, and magenta.
        final float[] triangle2VerticesData = {
                // X, Y, Z,
                // R, G, B, A
                -0.5f, -0.25f, 0.0f,
                1.0f, 1.0f, 0.0f, 1.0f,

                0.5f, -0.25f, 0.0f,
                0.0f, 1.0f, 1.0f, 1.0f,

                0.0f, 0.559016994f, 0.0f,
                1.0f, 0.0f, 1.0f, 1.0f};

        // This triangle is white, gray, and black.
        final float[] triangle3VerticesData = {
                // X, Y, Z,
                // R, G, B, A
                -0.5f, -0.25f, 0.0f,
                1.0f, 1.0f, 1.0f, 1.0f,

                0.5f, -0.25f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f,

                0.0f, 0.559016994f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f};

        //Above code's equivalent in ES1 looks like opengl C++ gc3
//        glBegin(GL_TRIANGLES);
//        glVertex3f(-0.5f, -0.25f, 0.0f);
//        glColor3f(1.0f, 0.0f, 0.0f);
//        ...
//        glEnd();

//        final float[] squareCoords[] = {
//
//        }

        final float squareCoords[] = {
                -0.5f,  0.5f, 0.0f,   // top left
                -0.5f, -0.5f, 0.0f,   // bottom left
                0.5f, -0.5f, 0.0f,   // bottom right
                0.5f,  0.5f, 0.0f    // top right?
        };

        short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

        // Initialize the buffers. for triangles
        // Initialize vertex byte buffer for shape coordinates
        // Use the device hardware's native byte order
        // Create a floating point buffer from the ByteBuffer
        mTriangle1Vertices = ByteBuffer.allocateDirect(triangle1VerticesData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        // Add the coordinates to the FloatBuffer
        // Set the buffer to read the first coordinate
        mTriangle1Vertices.put(triangle1VerticesData).position(0);

        mTriangle2Vertices = ByteBuffer.allocateDirect(triangle2VerticesData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        mTriangle2Vertices.put(triangle2VerticesData).position(0);

        mTriangle3Vertices = ByteBuffer.allocateDirect(triangle3VerticesData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        mTriangle3Vertices.put(triangle3VerticesData).position(0);


        //Square init
        mSquareVertices = ByteBuffer.allocateDirect(squareCoords.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        mSquareVertices.put(squareCoords).position(0);

        // initialize byte buffer for the draw list
        drawListBuffer = ByteBuffer.allocateDirect(drawOrder.length * 2)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        drawListBuffer.put(drawOrder).position(0);

        //Commented out code below is equivalent to above square init code
        // initialize vertex byte buffer for shape coordinates
//        ByteBuffer bb = ByteBuffer.allocateDirect(
//                // (# of coordinate values * 4 bytes per float)
//                squareCoords.length * 4);
//        bb.order(ByteOrder.nativeOrder());
//        mSquareVertices = bb.asFloatBuffer();
//        mSquareVertices.put(squareCoords);
//        mSquareVertices.position(0);
//
//        // initialize byte buffer for the draw list
//        ByteBuffer dlb = ByteBuffer.allocateDirect(
//                // (# of coordinate values * 2 bytes per short)
//                drawOrder.length * 2);
//        dlb.order(ByteOrder.nativeOrder());
//        drawListBuffer = dlb.asShortBuffer();
//        drawListBuffer.put(drawOrder);
//        drawListBuffer.position(0);
    }

    //The GL10 passed as parameters in below is only there since ES2 shares the same interface as ES1

    //This method is called when the surface if first created. It will also be called if
    //we lose out surface context and it is later recreated by the system
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        mSquare = new Square();

        // Set the background clear color to gray.
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

        // Position the eye behind the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 1.5f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        //There are three types of matrices model matrix, view matrix, and projection


        final String vertexShader =
                "uniform mat4 u_MVPMatrix;      \n"     // A constant representing the combined model/view/projection matrix.

                        + "attribute vec4 a_Position;     \n"     // Per-vertex position information we will pass in.
                        + "attribute vec4 a_Color;        \n"     // Per-vertex color information we will pass in.

                        + "varying vec4 v_Color;          \n"     // This will be passed into the fragment shader.

                        + "void main()                    \n"     // The entry point for our vertex shader.
                        + "{                              \n"
                        + "   v_Color = a_Color;          \n"     // Pass the color through to the fragment shader.
                        // It will be interpolated across the triangle.
                        + "   gl_Position = u_MVPMatrix   \n"     // gl_Position is a special variable used to store the final position.
                        + "               * a_Position;   \n"     // Multiply the vertex by the matrix to get the final point in
                        + "}                              \n";    // normalized screen coordinates.

        final String fragmentShader =
                "precision mediump float;       \n"     // Set the default precision to medium. We don't need as high of a
                        // precision in the fragment shader.
                        + "varying vec4 v_Color;          \n"     // This is the color from the vertex shader interpolated across the
                        // triangle per fragment.
                        + "void main()                    \n"     // The entry point for our fragment shader.
                        + "{                              \n"
                        + "   gl_FragColor = v_Color;     \n"     // Pass the color directly through the pipeline.
                        + "}                              \n";


        // Load in the vertex shader.
        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

        if (vertexShaderHandle != 0)
        {
            // Pass in the shader source.
            GLES20.glShaderSource(vertexShaderHandle, vertexShader);

            // Compile the shader.
            GLES20.glCompileShader(vertexShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0)
            {
                GLES20.glDeleteShader(vertexShaderHandle);
                vertexShaderHandle = 0;
            }
        }

        if (vertexShaderHandle == 0)
        {
            throw new RuntimeException("Error creating vertex shader.");
        }


        //Piece of code that was missing begin
        // Load in the fragment shader shader.
        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

        if (fragmentShaderHandle != 0)
        {
            // Pass in the shader source.
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);

            // Compile the shader.
            GLES20.glCompileShader(fragmentShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0)
            {
                GLES20.glDeleteShader(fragmentShaderHandle);
                fragmentShaderHandle = 0;
            }
        }

        if (fragmentShaderHandle == 0)
        {
            throw new RuntimeException("Error creating fragment shader.");
        }
        //end





        //Linking a vertex and fragment shader together into a program


        // Create a program object and store the handle to it.
        int programHandle = GLES20.glCreateProgram();

        if (programHandle != 0)
        {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle);

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            // Bind attributes
            GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
            GLES20.glBindAttribLocation(programHandle, 1, "a_Color");

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0)
            {
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if (programHandle == 0)
        {
            throw new RuntimeException("Error creating program.");
        }

        // Set program handles. These will later be used to pass in values to the program.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");

        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(programHandle);

        mSquare   = new Square();
    }

    //This is called whenever the surface changes; for example, when switching from portrait to landscape.
    //It is also called after the surface has been created.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    //This is called whenever it's time to draw a new frame
    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        // Do a complete rotation every 10 seconds.
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

//        drawSquare(mSquareVertices);



        // Draw the triangle facing straight on.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
        drawTriangle(mTriangle1Vertices);

        // Draw one translated a bit down and rotated to be flat on the ground.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, -1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 90.0f, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
        drawTriangle(mTriangle2Vertices);

        // Draw one translated a bit to the right and rotated to be facing to the left.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 90.0f, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
        drawTriangle(mTriangle3Vertices);

        mSquare.draw(mMVPMatrix);

    }
    /**
     * Draws a triangle from the given vertex data.
     *
     * @param aTriangleBuffer The buffer containing the vertex data.
     */
    private void drawTriangle(final FloatBuffer aTriangleBuffer)
    {
        // Pass in the position information
        aTriangleBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, aTriangleBuffer);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the color information
        aTriangleBuffer.position(mColorOffset);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, aTriangleBuffer);

        GLES20.glEnableVertexAttribArray(mColorHandle);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }

    //Draws brick on screen
    private void drawBrick() {

    }

    //Draws the ball
    private void drawBall() {

    }

    private void drawSquare(final FloatBuffer aTriangleBuffer)
    {
        // Pass in the position information
        aTriangleBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, aTriangleBuffer);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the color information
        aTriangleBuffer.position(mColorOffset);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, aTriangleBuffer);

        GLES20.glEnableVertexAttribArray(mColorHandle);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        short drawOrder[] = { 0, 1, 2, 0, 2, 3};
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
    }

    /**
     * Utility method for compiling a OpenGL shader.
     *
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}
