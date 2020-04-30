const express = require('express');
const mongoose = require('mongoose');
const bodyParser = require('body-parser');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const app = express();
var request = require('sync-request');
var request_prom = require('then-request');

const saltRounds = 12;
const jwtSecret = 'qwer4wdk4!09%C';
const toker = 'ParallelMiniProject';
const second = 'https://poomrokc.services/chat/';

function getRandomInt(max) {
  return Math.floor(Math.random() * Math.floor(max));
}

app.use(bodyParser.json());

mongoose.connect('mongodb://localhost:27017/chat2', {useNewUrlParser: true, useUnifiedTopology: true});

var UserModel = require('./models/User');
var UserGroupModel = require('./models/UserGroup');
var GroupModel = require('./models/Group');
var ChatModel = require('./models/Chat');

const backup =async(dis)=> {
	await UserModel.deleteMany({});
	await GroupModel.deleteMany({});
	await ChatModel.deleteMany({});
	await UserGroupModel.deleteMany({});
	
	await UserModel.create(dis.users);
	await GroupModel.create(dis.groups);
	await ChatModel.create(dis.chats);
	await UserGroupModel.create(dis.usergroups);
};
//backup
var suc=false;
while(!suc) {
	try {
		var res = request('POST', second+'dump', {
		  json: {password: toker},
		});
		var data = JSON.parse(res.getBody('utf8'));
		if(data.success) {
			suc=true;
			var dis = data.data;
			for(var i=0;i<dis.users.length;i++)
				dis.users[i]._id = mongoose.Types.ObjectId(dis.users[i]._id);
			for(var i=0;i<dis.groups.length;i++)
				dis.groups[i]._id = mongoose.Types.ObjectId(dis.groups[i]._id);
			for(var i=0;i<dis.chats.length;i++) {
				dis.chats[i]._id = mongoose.Types.ObjectId(dis.chats[i]._id);
				dis.chats[i].user = mongoose.Types.ObjectId(dis.chats[i].user);
				dis.chats[i].group = mongoose.Types.ObjectId(dis.chats[i].group);
			}
			for(var i=0;i<dis.usergroups.length;i++) {
				dis.usergroups[i]._id = mongoose.Types.ObjectId(dis.usergroups[i]._id);
				dis.usergroups[i].user = mongoose.Types.ObjectId(dis.usergroups[i].user);
				dis.usergroups[i].group = mongoose.Types.ObjectId(dis.usergroups[i].group);
			}
			backup(dis);
		}
	} catch(err) {
	}
}


const jwtMiddleWare = async(req, res, next) => {
	try {
		let auth = req.header('Authorization').split(' ')[1];
		var decoded = jwt.verify(auth, jwtSecret);
		let user = await UserModel.findOne({username:decoded.username}, '-password');
		req.user = user;
		next();
	}
	catch(err) {
		res.status(401).send('Unauthorized');
	}
};

app.use((req,res,next)=>{
	res.set('Active-Server', 'MAIN');
	next();
});

app.use('/images', express.static('images'));

app.get('/', (req, res) => {
	res.json('Welcome to chat backend');
});

app.post('/register', async(req, res) => {
	try {
		let hashed = await bcrypt.hash(req.body.password, saltRounds);
		let photo = 'https://hueco.ml/chat/images/'+getRandomInt(8)+'.jpg';
		let insert = {
			username: req.body.username,
			password: hashed,
			name: req.body.name,
			photo
		};
		let exist = await UserModel.findOne({username:req.body.username});
		if(exist)
			return res.status(400).send('Username already exist');
		
		var uuu = await UserModel.create(insert);
		var jsondata = {password:toker,data:uuu};
		request_prom('POST', second+'bregister', {json: jsondata}).getBody('utf8').then(JSON.parse).done(function (res) {

		});
		
		
		let token = jwt.sign({username:req.body.username}, jwtSecret);
		res.json({suceess:true,token});
	} catch(err) {
		res.status(400).send('Bad Request');
	}
});

app.post('/login', async(req, res) => {
	try {
		let exist = await UserModel.findOne({username:req.body.username});
		if(!exist)
			return res.status(404).send('Username not found');
		let result = await bcrypt.compare(req.body.password, exist.password);
		if(!result)
			return res.status(400).send('Password does not match');
		let token = jwt.sign({username:req.body.username}, jwtSecret);
		res.json({suceess:true,token});
	} catch(err) {
		res.status(400).send('Bad Request');
	}
});

app.get('/profile', jwtMiddleWare, async(req, res) => {
	try {
		res.json(req.user);
	} catch(err) {
		res.status(400).send('Bad Request');
	}
});  
 
app.post('/createGroup', jwtMiddleWare, async(req, res) => {
	try {
		let photo = 'https://hueco.ml/chat/images/'+getRandomInt(8)+'.jpg';
		var uuu = await GroupModel.create({name: req.body.name,photo});
		var vvv = await UserGroupModel.create({user: req.user._id, group: uuu._id});
		
		var jsondata = {password:toker,data:uuu};
		request_prom('POST', second+'bgroup', {json: jsondata}).getBody('utf8').then(JSON.parse).done(function (res) {

		});
		
		var jsondata2 = {password:toker,data:vvv};
		request_prom('POST', second+'bgu', {json: jsondata2}).getBody('utf8').then(JSON.parse).done(function (res) {
 
		});
		
		res.json({success:true});
	} catch(err) {
		console.log(err);
		res.status(400).send('Bad Request');
	}
});

app.post('/joinGroup', jwtMiddleWare, async(req, res) => {
	try {
		let group = await GroupModel.findOne({_id: mongoose.Types.ObjectId(req.body.groupID)});
		if(!group)
			return res.status(404).send('Group not found');
		let exist = await UserGroupModel.findOne({user: req.user._id, group: group._id});
		if(exist)
			return res.status(400).send('You already in the group');
		var vvv = await UserGroupModel.create({user: req.user._id, group: group._id});
		
		var jsondata2 = {password:toker,data:vvv};
		request_prom('POST', second+'bgu', {json: jsondata2}).getBody('utf8').then(JSON.parse).done(function (res) {
 
		});
		res.json({success:true});
	} catch(err) {
		res.status(400).send('Bad Request');  
	}
});

app.post('/leaveGroup', jwtMiddleWare, async(req, res) => {
	try {
		let group = await GroupModel.findOne({_id: mongoose.Types.ObjectId(req.body.groupID)});
		if(!group)
			return res.status(404).send('Group not found');
		await UserGroupModel.deleteOne({user: req.user._id, group: group._id});
		
		var jsondata2 = {password:toker,data:{user: req.user._id, group: group._id}};
		request_prom('POST', second+'blg', {json: jsondata2}).getBody('utf8').then(JSON.parse).done(function (res) {
 
		});
		res.json({success:true});
	} catch(err) { 
		res.status(400).send('Bad Request');
	}
}); 

app.post('/chat', jwtMiddleWare, async(req, res) => {
	try {
		let group = await GroupModel.findOne({_id: mongoose.Types.ObjectId(req.body.groupID)});
		if(!group)
			return res.status(404).send('Group not found'); 
		let exist = await UserGroupModel.findOne({user: req.user._id, group: group._id});
		if(!exist)
			return res.status(401).send('You are not in the group');
		
		var vvv = await ChatModel.create({group: group._id, user: req.user._id, message:req.body.message});
		
		var jsondata = {password:toker,data:vvv};
		request_prom('POST', second+'bchat', {json: jsondata}).getBody('utf8').then(JSON.parse).done(function (res) {

		});
		
		res.json({success:true});
	} catch(err) {
		res.status(400).send('Bad Request');
	}
});

app.get('/mygroups', jwtMiddleWare, async(req, res) => {
	try {
		let groups = await UserGroupModel.find({user:req.user._id});
		let data = [];
		for(let i=0;i<groups.length;i++) {
			let g = {members:[]};
			let group = await GroupModel.findOne({_id:groups[i].group});
			g.name = group.name;
			g._id = group._id;
			g.photo = group.photo;
			let memb = await UserGroupModel.find({group:group._id});
			for(let m=0;m<memb.length;m++) {
				let member = await UserModel.findOne({_id:memb[m].user});
				g.members.push({_id:member._id,photo:member.photo,name:member.name});
			}
			data.push(g);
		}
		res.json({sucess:true,data});
	} catch(err) {
		res.status(400).send('Bad Request');
	}
});

app.get('/mychat', jwtMiddleWare, async(req, res) => {
	try {
		let groupID = mongoose.Types.ObjectId(req.query.groupID);
		let timeStamp = new Date(req.query.timeStamp);
		let exist = await UserGroupModel.findOne({group:groupID});
		if(!exist)
			return res.status(401).send('You are not in the group');
		let chats = await ChatModel.find({group:groupID,created:{$gt:timeStamp}}).sort({created:1});
		res.json({success:true,data:chats});
	} catch(err) {
		res.status(400).send('Bad Request');
	}
});

app.listen(9999, () => {
	console.log('Start server at port 9999.');
});