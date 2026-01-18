# Render Deployment Checklist - Step by Step

## ✅ Pre-Deployment Checklist

### Code Ready
- [x] Migration to PostgreSQL completed
- [x] `render.yaml` configured
- [x] `Dockerfile` optimized
- [x] `application-prod.yaml` updated
- [x] Local test passed

### Environment Variables Ready
- [x] All values collected from `.env` file
- [x] Database connection format verified

## 🚀 Deployment Steps

### Step 1: Commit and Push Code

```powershell
# Check current status
git status

# Add all changes
git add .

# Commit
git commit -m "Migrate to PostgreSQL and prepare for Render deployment"

# Push to repository
git push origin main
```

### Step 2: Create Render Account

1. Go to: https://dashboard.render.com
2. Sign up/Login with GitHub or GitLab account
3. Authorize Render to access your repositories

### Step 3: Create PostgreSQL Database

1. In Render Dashboard, click **"New +"** → **"PostgreSQL"**
2. Configure:
   - **Name**: `coiviet-db`
   - **Database**: (auto-generated)
   - **User**: (auto-generated)
   - **Region**: `Singapore` (or closest to you)
   - **PostgreSQL Version**: `16` (or latest)
   - **Plan**: 
     - Free (limited, for testing)
     - Starter ($7/month, recommended for production)
3. Click **"Create Database"**
4. **Wait for database to be ready** (takes 1-2 minutes)
5. **Save the connection details** (you'll see them in the dashboard)

### Step 4: Create Web Service

1. In Render Dashboard, click **"New +"** → **"Web Service"**
2. **Connect Repository:**
   - Select your Git provider (GitHub/GitLab)
   - Authorize if needed
   - Select repository: `coiviet` (or your repo name)
   - Select branch: `main` (or `master`)
3. **Configure Service:**
   - **Name**: `coiviet-api`
   - **Region**: `Singapore` (same as database)
   - **Branch**: `main`
   - **Root Directory**: (leave empty, or `./` if needed)
   - **Runtime**: `Docker`
   - **Dockerfile Path**: `./Dockerfile`
   - **Docker Context**: `.`
   - **Plan**: 
     - Free (sleeps after 15 min inactivity)
     - Starter ($7/month, no sleep)
4. **Advanced Settings:**
   - **Health Check Path**: `/actuator/health`
   - **Auto-Deploy**: `Yes` (deploy on every push)
5. Click **"Create Web Service"**

### Step 5: Link Database to Web Service

1. In your Web Service settings
2. Go to **"Environment"** tab
3. Scroll down to **"Add Environment Variable"** or **"Link Database"**
4. Click **"Link Database"** or **"Add"** → **"Database"**
5. Select: `coiviet-db`
6. Render will automatically add these variables:
   - `DATABASE_URL`
   - `DATABASE_HOST`
   - `DATABASE_PORT`
   - `DATABASE_NAME`
   - `DATABASE_USER`
   - `DATABASE_PASSWORD`

### Step 6: Set Environment Variables

In Web Service → **Environment** tab, add these variables:

#### Database (Format JDBC URL)
```
Key: DBMS_CONNECTION
Value: jdbc:postgresql://${DATABASE_HOST}:${DATABASE_PORT}/${DATABASE_NAME}?sslmode=require
```

```
Key: DBMS_USERNAME
Value: ${DATABASE_USER}
```

```
Key: DBMS_PASSWORD
Value: ${DATABASE_PASSWORD}
```

#### Application
```
Key: SPRING_PROFILES_ACTIVE
Value: prod
```

#### JWT
```
Key: JWT_SIGNER_KEY
Value: 3aF+lAiyA/tEAeeBtmlou0RwdTwXx0lU6SjH0MYBR7DRt9vyJzlv66uqnqHMP2NW
```

```
Key: JWT_VALID_DURATION
Value: 86400
```

```
Key: JWT_REFRESHABLE_DURATION
Value: 36000
```

#### Cloudinary
```
Key: CLOUDINARY_CLOUD_NAME
Value: dcs0lhrvh
```

```
Key: CLOUDINARY_API_KEY
Value: 718451452685618
```

```
Key: CLOUDINARY_API_SECRET
Value: GXhU99xN-CpagV9OBgT6R2PipyQ
```

#### Email
```
Key: MAIL_USERNAME
Value: truongltse180010@fpt.edu.vn
```

```
Key: MAIL_PASSWORD
Value: zhvr axud xxnb jihr
```

#### MoMo Payment
```
Key: MOMO_PARTNER_CODE
Value: MOMOBKUN20180529
```

```
Key: MOMO_ACCESS_KEY
Value: klm05TvNBzhg7h7j
```

```
Key: MOMO_SECRET_KEY
Value: at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa
```

```
Key: MOMO_REDIRECT_URL
Value: https://coiviet-api.onrender.com/api/public/payment/momo-return
```
**Note:** Update this after you get the actual URL

```
Key: MOMO_NOTIFY_URL
Value: https://coiviet-api.onrender.com/api/public/payment/momo-notify
```
**Note:** Update this after you get the actual URL

#### Google OAuth2
```
Key: GOOGLE_CLIENT_ID
Value: 87846938671-76pcjrb3ucf7ngmkai7b2qni7uvrn9qt.apps.googleusercontent.com
```

```
Key: GOOGLE_CLIENT_SECRET
Value: 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
```

```
Key: GOOGLE_REDIRECT_URI
Value: https://coiviet-api.onrender.com/login/oauth2/code/google
```
**Note:** Update this after you get the actual URL, and add to Google Console

```
Key: OAUTH2_REDIRECT_SUCCESS
Value: https://your-frontend-domain.com/oauth2/callback
```
**Note:** Update with your frontend URL

#### Admin
```
Key: INITIAL_ADMIN_PASSWORD
Value: admin123
```

### Step 7: Deploy

1. After setting all environment variables, click **"Save Changes"**
2. Go to **"Manual Deploy"** tab
3. Click **"Deploy latest commit"**
4. **Wait for build** (takes 5-10 minutes for first build)
5. Monitor build logs

### Step 8: Verify Deployment

1. **Check Build Logs:**
   - Should see: "BUILD SUCCESS"
   - Should see: "Started CoivietApplication"
   - No database connection errors

2. **Check Health Endpoint:**
   - URL: `https://coiviet-api.onrender.com/actuator/health`
   - Should return: `{"status":"UP"}`

3. **Check Swagger UI:**
   - URL: `https://coiviet-api.onrender.com/swagger-ui.html`
   - Should load successfully

4. **Test API Endpoints:**
   - Try a few endpoints to verify they work

### Step 9: Update URLs (After Getting Production URL)

1. **Get your production URL:**
   - In Render Dashboard → Web Service
   - You'll see: `https://coiviet-api.onrender.com` (or similar)

2. **Update MoMo URLs:**
   - In Render Dashboard → Environment
   - Update `MOMO_REDIRECT_URL` with actual URL
   - Update `MOMO_NOTIFY_URL` with actual URL

3. **Update Google OAuth2:**
   - Update `GOOGLE_REDIRECT_URI` with actual URL
   - Go to Google Cloud Console: https://console.cloud.google.com
   - APIs & Services → Credentials
   - Edit OAuth 2.0 Client
   - Add to "Authorized redirect URIs":
     - `https://coiviet-api.onrender.com/login/oauth2/code/google`
   - Save changes

4. **Redeploy** (if needed):
   - Changes to environment variables require redeploy
   - Go to "Manual Deploy" → "Deploy latest commit"

## ⚠️ Common Issues & Solutions

### Build Fails
- Check build logs for errors
- Verify Java 21 is supported
- Check Maven dependencies

### Database Connection Fails
- Verify `DBMS_CONNECTION` format is correct
- Check database is linked properly
- Verify `sslmode=require` in JDBC URL

### Health Check Fails
- Check `/actuator/health` endpoint is accessible
- Verify application started successfully
- Check application logs

### Application Crashes
- Check application logs
- Verify all environment variables are set
- Check database connection

## 📝 Notes

- **Free Tier**: Web service sleeps after 15 minutes of inactivity
- **First Request**: May take 30-60 seconds to wake up
- **Build Time**: First build takes 5-10 minutes, subsequent builds are faster
- **Database**: Free tier has limitations (storage, connections)

## ✅ Post-Deployment Checklist

- [ ] Build successful
- [ ] Health endpoint returns UP
- [ ] Swagger UI accessible
- [ ] API endpoints working
- [ ] Database schema created
- [ ] MoMo URLs updated
- [ ] Google OAuth2 redirect URI updated
- [ ] All services working correctly

## 🎉 Success!

Once all checks pass, your application is live on Render!

**Your API URL:** `https://coiviet-api.onrender.com`
