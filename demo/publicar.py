# Publica una cerveza como un usuario de ejemplo (para probar la notificación en directo)
# uso: python demo/publicar.py app/google-services.json ana "Nombre" Estilo 4 "Lugar" -34.585 -58.43
import json, sys, uuid, datetime, urllib.request, urllib.error
cfg=json.load(open(sys.argv[1])); KEY=cfg["client"][0]["api_key"][0]["current_key"]; P=cfg["project_info"]["project_id"]
B=f"https://firestore.googleapis.com/v1/projects/{P}/databases/(default)/documents"
user,nombre,estilo,punt,lugar,lat,lng=sys.argv[2],sys.argv[3],sys.argv[4],int(sys.argv[5]),sys.argv[6],float(sys.argv[7]),float(sys.argv[8])
def req(u,d=None,t=None,m=None):
    h={"Content-Type":"application/json"}
    if t:h["Authorization"]="Bearer "+t
    try:return json.load(urllib.request.urlopen(urllib.request.Request(u,json.dumps(d).encode() if d else None,h,method=m)))
    except urllib.error.HTTPError as e:return {"error":e.code,"b":e.read().decode()[:200]}
email=f"{user}@aplicaciones.com"
a=req(f"https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key={KEY}",{"email":email,"password":"cervelog123","returnSecureToken":True})
t,uid=a["idToken"],a["localId"]
cid=uuid.uuid4().hex[:20]
ahora={"timestampValue":datetime.datetime.now(datetime.timezone.utc).isoformat().replace("+00:00","Z")}
comun={"nombre":{"stringValue":nombre},"estilo":{"stringValue":estilo},"puntuacion":{"integerValue":str(punt)},
       "latitud":{"doubleValue":lat},"longitud":{"doubleValue":lng},"fecha":ahora}
r1=req(f"{B}/usuarios/{uid}/cervezas/{cid}",{"fields":{**comun,"cerveceria":{"stringValue":""},"lugar":{"stringValue":lugar},"notas":{"stringValue":""}}},t,"PATCH")
r2=req(f"{B}/publicaciones/{cid}",{"fields":{**comun,"autorUid":{"stringValue":uid},"autorEmail":{"stringValue":email}}},t,"PATCH")
print(user,nombre,"OK" if "error" not in r1 and "error" not in r2 else (r1,r2))
